/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2018-2023 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.daporkchop.savesearcher;

import net.daporkchop.lib.common.function.io.IOConsumer;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.lib.common.system.OperatingSystem;
import net.daporkchop.lib.common.system.PlatformInfo;
import net.daporkchop.lib.compression.zlib.Zlib;
import net.daporkchop.lib.logging.LogAmount;
import net.daporkchop.lib.math.vector.i.Vec2i;
import net.daporkchop.lib.minecraft.region.WorldScanner;
import net.daporkchop.lib.minecraft.region.util.ChunkProcessor;
import net.daporkchop.lib.minecraft.region.util.NeighboringChunkProcessor;
import net.daporkchop.lib.minecraft.tileentity.TileEntityRegistry;
import net.daporkchop.lib.minecraft.world.MinecraftSave;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.lib.minecraft.world.format.anvil.AnvilSaveFormat;
import net.daporkchop.lib.minecraft.world.format.anvil.AnvilWorldManager;
import net.daporkchop.lib.minecraft.world.format.anvil.region.RegionFile;
import net.daporkchop.lib.minecraft.world.format.anvil.region.RegionOpenOptions;
import net.daporkchop.lib.minecraft.world.impl.MinecraftSaveConfig;
import net.daporkchop.lib.minecraft.world.impl.SaveBuilder;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.module.impl.chest.ChestModule;
import net.daporkchop.savesearcher.module.impl.entity.EntityModule;
import net.daporkchop.savesearcher.module.impl.BrokenLightingModule;
import net.daporkchop.savesearcher.module.impl.BrokenPortalModule;
import net.daporkchop.savesearcher.module.impl.CommandBlockModule;
import net.daporkchop.savesearcher.module.impl.EmptyChunksModule;
import net.daporkchop.savesearcher.module.impl.biome.BiomeModule;
import net.daporkchop.savesearcher.module.impl.NetherChunksModule;
import net.daporkchop.savesearcher.module.impl.SignModule;
import net.daporkchop.savesearcher.module.impl.SpawnerModule;
import net.daporkchop.savesearcher.module.impl.block.BlockModule;
import net.daporkchop.savesearcher.module.impl.count.CountBlocksModule;
import net.daporkchop.savesearcher.module.impl.tileentity.TileEntityModule;
import net.daporkchop.savesearcher.output.OutputHandle;
import net.daporkchop.savesearcher.output.csv.CSVOutputHandle;
import net.daporkchop.savesearcher.output.csv.CompressedCSVOutputHandle;
import net.daporkchop.savesearcher.tileentity.TileEntityCommandBlock;
import net.daporkchop.savesearcher.tileentity.TileEntitySpawner;
import net.daporkchop.savesearcher.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.daporkchop.lib.logging.Logging.*;

/**
 * @author DaPorkchop_
 */
public class Main {
    private static final Map<String, Function<String[], SearchModule>> REGISTERED_MODULES = new HashMap<String, Function<String[], SearchModule>>() {
        {
            this.put("--block", BlockModule::find);
            this.put("--count", CountBlocksModule::find);
            this.put("--chest", ChestModule::find);
            this.put("--doublechest", args -> {
                logger.warn("The --doublechest module was requested, which is no longer supported! Use --chest,double=true.");
                return ChestModule.find(Stream.concat(Stream.of(args), Stream.of("double=true")).toArray(String[]::new));
            });
            this.put("--emptychunks", EmptyChunksModule::new);
            this.put("--entity", EntityModule::find);
            this.put("--biomes", BiomeModule::find);
            this.put("--netherchunks", NetherChunksModule::new);
            this.put("--brokenportals", BrokenPortalModule::new);
            this.put("--spawner", SpawnerModule::new);
            this.put("--sign", SignModule::new);
            this.put("--tileentity", TileEntityModule::find);
            this.put("--command_block", CommandBlockModule::new);
            this.put("--brokenlighting", BrokenLightingModule::new);
        }
    };

    private static final Map<String, Function<File, OutputHandle>> REGISTERED_OUTPUTS = new HashMap<String, Function<File, OutputHandle>>() {
        {
            this.put("csv", CSVOutputHandle::new);
            this.put("csv_gz", CompressedCSVOutputHandle::new);
        }
    };

    public static void main(String... args) throws IOException {
        logger.setLogAmount(LogAmount.DEBUG).enableANSI();

        Thread.currentThread().setUncaughtExceptionHandler((thread, ex) -> {
            logger.alert(ex);
            System.exit(1);
        });

        if (!Zlib.PROVIDER.isNative()) {
            logger.warn("Native zlib couldn't be loaded! Only supported on x86_64-linux-gnu, x86-linux-gnu and x86_64-w64-mingw32")
                    .warn("This could have significant performance implications!");
        }

        if (args.length == 0
            || contains(args, "-h")
            || contains(args, "--help")
            || contains(args, "--h")
            || contains(args, "-help")) {
            logger.info("SaveSearcher v%s", Version.VERSION)
                    .info("Copyright (c) DaPorkchop_")
                    .info("https://github.com/DaMatrix/SaveSearcher")
                    .info("")
                    .info("--input=<path>                      Sets the input world path (required)")
                    .info("--dim=<dimension id>                Sets the dimension (world) id to scan. default=0")
                    .info("--verbose                           Print status updates to console")
                    .info("--format=<format>                   Sets the format that the output data will be written in. valid formats=csv,csv_gz default=csv")
                    .info("--output=<path>                     Set the root directory that output data will be written to. default=./scanresult/")
                    .info("--minChunkX=<value>                 Limits the area of the world to scan by setting the minimum/maximum X/Z coordinate to scan. Both minimum and")
                    .info("--maxChunkX=<value>                   maximum values are inclusive, and are measured in chunks. If unset, the minimum/maximum values default to")
                    .info("--minChunkZ=<value>                   negative/positive infinity, respectively.")
                    .info("--maxChunkZ=<value>")
                    .info("")
                    .info("MODULES")
                    .info("--block,id=<id>(,meta=<meta>)       Scan for a certain block id+meta, saving coordinates. Block ids should be in format 'minecraft:stone'. Meta must be 0-15, by default")
                    .info("      (,min=<min>)(,max=<max>)        it's ignored. Both min and max values are inclusive, and default to min=0 and max=255 if not given. Adding the invert flag will cause")
                    .info("      (,invert=<true/false>)          a search for block coordinates where the given block id+meta does not occur. Adding the chunkinvert flag will cause a search for chunk")
                    .info("      (,chunkinvert=<true/false>)     coordinates where the given block id+meta does not occur. invert and chunkinvert may not be used together.")
                    .info("--count,type=<type>(,id=<id>)       Counts the number of occurrences of the given type in each chunk, saving chunk coordinates and count. Valid types: block, tileentity. id")
                    .info("      (,meta=<meta>)                  is required for block, optional for tileentity. meta is optional for block, not allowed for tileentity.")
                    .info("--chest(,double=<true/false>)       Scan for (double) chests, saving coordinates and whether or not they're trapped, and optionally the ID+meta of the block above them.")
                    .warn("      (,above=<true/false>)           WARNING! Can cause significant slowdown!")
                    .info("--netherchunks                      Scan for nether chunks that have somehow ended up in the overworld.")
                    .info("--emptychunks                       Scan for empty (air-only) chunks.")
                    .info("--brokenportals                     Scan for portals that aren't supported by an obsidian frame.")
                    .info("--sign(,mode=<mode>)                Scan for sign blocks, saving coordinates and text. Valid modes: plain_text (default), formatted_legacy, raw.")
                    .info("--spawner(,<id>)                    Scan for spawner blocks, optionally filtering based on mob type and saving coordinates and entity type.")
                    .info("--entity(,<id>)                     Scan for entities, optionally filtering based on entity ID and saving coordinates and NBT data.")
                    .info("--biomes(,<id>)                     Scan for biomes, optionally filtering based on biome ID and saving coordinates.")
                    .info("--tileentity(,<id>)                 Scan for tile-entities, optionally filtering based on entity ID and saving coordinates and NBT data.")
                    .info("--command_block(,<command_regex>)   Scan for command blocks, optionally filtering based on commands that match a given regex and saving coordinates, command, and last output.")
                    .info("--brokenlighting                    Scan for chunks with broken sky lighting, saving chunk coordinates. Can optionally create a PendingLight.dat file compatible")
                    .info("       (,lightcleaner=<path>)         with LightCleaner (Spigot plugin).");

            return;
        } else {
            logger.addFile(new File("savesearcher.log").getAbsoluteFile(), true, LogAmount.DEBUG)
                    .info("SaveSearcher v%s", Version.VERSION)
                    .info("Copyright (c) DaPorkchop_")
                    .info("https://github.com/DaMatrix/SaveSearcher")
                    .info("")
                    .info("Starting...");
        }

        if (PlatformInfo.OPERATING_SYSTEM == OperatingSystem.Windows) {
            logger.alert("Windows detected!\nUsing stupid file names because your operating system is too stupid to handle good ones...");
        }

        File worldFile = null;
        File outDir = new File("scanresult");
        int dim = 0;
        ScanRange range = new ScanRange();
        boolean verbose = false;
        boolean overwrite = false;
        String formatName = "csv";
        List<SearchModule> modules = new ArrayList<>();
        for (String s : args) {
            if (s.isEmpty()) {
                continue;
            }
            String[] split = s.split("=");
            switch (split[0]) {
                case "--input":
                case "-i":
                    worldFile = new File(split[1]);
                    continue;
                case "--format":
                case "-f":
                    formatName = split[1];
                    continue;
                case "--output":
                    outDir = new File(split[1]);
                    continue;
                case "--overwrite":
                case "-o":
                    overwrite = true;
                    continue;
                case "--dim":
                    dim = Integer.parseInt(split[1]);
                    continue;
                case "--verbose":
                case "-v":
                    verbose = true;
                    continue;
                case "--minChunkX":
                    range.regionMinX = (range.chunkMinX = Integer.parseInt(split[1])) >> 5;
                    continue;
                case "--maxChunkX":
                    range.regionMaxX = (range.chunkMaxX = Integer.parseInt(split[1])) >> 5;
                    continue;
                case "--minChunkZ":
                    range.regionMinZ = (range.chunkMinZ = Integer.parseInt(split[1])) >> 5;
                    continue;
                case "--maxChunkZ":
                    range.regionMaxZ = (range.chunkMaxZ = Integer.parseInt(split[1])) >> 5;
                    continue;
            }
            split = s.split(",");
            Function<String[], SearchModule> function = REGISTERED_MODULES.get(split[0]);
            if (function == null) {
                logger.error("Invalid module: %s", split[0]);
                System.exit(1);
            }
            SearchModule module = function.apply(Arrays.copyOfRange(split, 1, split.length));
            if (modules.contains(module)) {
                logger.warn("Duplicate argument: \"%s\"!", s);
            } else {
                modules.add(module);
            }
        }

        if (worldFile == null) {
            logger.error("World path not set!");
            System.exit(1);
        } else if (modules.isEmpty()) {
            logger.error("No modules enabled!");
            System.exit(1);
        }

        if (PFiles.checkDirectoryExists(outDir)) {
            if (overwrite) {
                logger.warn("Deleting contents of \"%s\" as -o is enabled...", outDir.getAbsolutePath());
                PFiles.rmContents(outDir);
            } else if (outDir.list().length != 0) {
                logger.error("Output directory \"%s\" is not empty!", outDir.getAbsolutePath())
                        .error("Use -o to forcibly delete existing files, or delete them manually.");
                System.exit(1);
            }
        }

        if (!REGISTERED_OUTPUTS.containsKey(formatName)) {
            logger.error("Unknown output format: \"%s\"!")
                    .error("Valid output formats are:");
            REGISTERED_OUTPUTS.forEach((name, factory) -> logger.error("  %s", name));
            System.exit(1);
        }

        logger.info("Beginning scan of world %s with %d modules enabled.", worldFile.getAbsolutePath(), modules.size())
                .info("Modules:");
        modules.forEach(m -> logger.info("  %s", m.toString()));

        long time = System.currentTimeMillis();
        LongAdder count = new LongAdder();
        Set<Vec2i> regionPositions = ConcurrentHashMap.newKeySet();
        try (MinecraftSave save = new SaveBuilder()
                .setInitFunctions(new MinecraftSaveConfig()
                        .openOptions(new RegionOpenOptions().access(RegionFile.Access.READ_ONLY).mode(RegionFile.Mode.MMAP_FULL))
                        .tileEntityFactory(TileEntityRegistry.builder(TileEntityRegistry.defaultRegistry())
                                .add(TileEntityCommandBlock.ID, TileEntityCommandBlock::new)
                                .add(TileEntitySpawner.ID, TileEntitySpawner::new)
                                .build()))
                .setFormat(new AnvilSaveFormat(worldFile)).build()) {
            World world = save.world(dim);
            if (world == null) {
                throw new IllegalArgumentException(String.format("Invalid dimension: %d", dim));
            }

            for (SearchModule module : modules) {
                module.init(world, REGISTERED_OUTPUTS.get(formatName).apply(outDir));
            }

            List<SearchModule> mergedModules = SearchModule.merge(modules, SearchModule::merge);

            WorldScanner scanner = new WorldScanner(world) {
                @Override
                public WorldScanner addProcessor(ChunkProcessor processor) {
                    if (processor instanceof NeighboringChunkProcessor) {
                        return super.addProcessor((NeighboringChunkProcessor) processor);
                    } else {
                        return super.addProcessor(processor);
                    }
                }

                @Override
                protected Collection<Vec2i> getRegionPositions(AnvilWorldManager anvilWorldManager) {
                    Collection<Vec2i> regions = super.getRegionPositions(anvilWorldManager);
                    regions.removeIf(range::excludesRegion);
                    return regions;
                }

                @Override
                protected void maskVisitableChunksInRegion(int regionX, int regionZ, int baseChunkX, int baseChunkZ, BitSet mask) {
                    super.maskVisitableChunksInRegion(regionX, regionZ, baseChunkX, baseChunkZ, mask);

                    if (range.excludesRegion(regionX, regionZ)) { //the region isn't being processed, skip it entirely
                        mask.clear();
                        return;
                    }

                    //skip chunks outside the configured range
                    for (int x = 0; x < 32; x++) {
                        for (int z = 0; z < 32; z++) {
                            if (range.excludesChunk(baseChunkX + x, baseChunkZ + z)) {
                                mask.clear(x * 32 + z);
                            }
                        }
                    }
                }
            };
            if (verbose) {
                scanner.addProcessor((current, estimatedTotal, column) -> {
                    if (regionPositions.add(new Vec2i(column.getX() >> 5, column.getZ() >> 5))) {
                        logger.debug("Processing region #%d (%d,%d), chunk %d/~%d (%.2f%%)", regionPositions.size(), column.getX() >> 5, column.getZ() >> 5, current, estimatedTotal, ((double) current / (double) estimatedTotal) * 100.0d);
                    }
                    count.increment();
                });
            } else {
                scanner.addProcessor((current, estimatedTotal, column) -> {
                    regionPositions.add(new Vec2i(column.getX() >> 5, column.getZ() >> 5));
                    count.increment();
                });
            }
            mergedModules.forEach(scanner::addProcessor);
            scanner.run(true);

            logger.info("Finishing...");
            mergedModules.forEach((IOConsumer<SearchModule>) SearchModule::close);
        }
        time = System.currentTimeMillis() - time;
        logger.success("Done!").success(
                "Scanned %d chunks (across %d regions) in %dh:%dm:%ds",
                count.sum(),
                regionPositions.size(),
                time / (1000L * 60L * 60L),
                time / (1000L * 60L) % 60,
                time / (1000L) % 60
        );
    }

    private static boolean contains(String[] arr, String s) {
        for (String s1 : arr) {
            if (s1.equalsIgnoreCase(s)) {
                return true;
            }
        }

        return false;
    }

    private static final class ScanRange {
        public int chunkMinX = Integer.MIN_VALUE;
        public int chunkMaxX = Integer.MAX_VALUE;
        public int chunkMinZ = Integer.MIN_VALUE;
        public int chunkMaxZ = Integer.MAX_VALUE;

        public int regionMinX = Integer.MIN_VALUE;
        public int regionMaxX = Integer.MAX_VALUE;
        public int regionMinZ = Integer.MIN_VALUE;
        public int regionMaxZ = Integer.MAX_VALUE;

        public boolean excludesRegion(Vec2i regionPos) {
            return this.excludesRegion(regionPos.getX(), regionPos.getY());
        }

        public boolean excludesRegion(int regionX, int regionZ) {
            return regionX < this.regionMinX || regionX > this.regionMaxX || regionZ < this.regionMinZ || regionZ > this.regionMaxZ;
        }

        public boolean excludesChunk(int chunkX, int chunkZ) {
            return chunkX < this.chunkMinX || chunkX > this.chunkMaxX || chunkZ < this.chunkMinZ || chunkZ > this.chunkMaxZ;
        }
    }
}
