/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2018-2019 DaPorkchop_ and contributors
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it. Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.savesearcher;

import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.lib.logging.LogAmount;
import net.daporkchop.lib.logging.Logging;
import net.daporkchop.lib.math.vector.i.Vec2i;
import net.daporkchop.lib.minecraft.region.WorldScanner;
import net.daporkchop.lib.minecraft.region.util.ChunkProcessor;
import net.daporkchop.lib.minecraft.region.util.NeighboringChunkProcessor;
import net.daporkchop.lib.minecraft.tileentity.TileEntityRegistry;
import net.daporkchop.lib.minecraft.world.MinecraftSave;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.lib.minecraft.world.format.anvil.AnvilSaveFormat;
import net.daporkchop.lib.minecraft.world.format.anvil.region.RegionFile;
import net.daporkchop.lib.minecraft.world.format.anvil.region.RegionOpenOptions;
import net.daporkchop.lib.minecraft.world.impl.MinecraftSaveConfig;
import net.daporkchop.lib.minecraft.world.impl.SaveBuilder;
import net.daporkchop.lib.natives.PNatives;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.module.impl.SignModule;
import net.daporkchop.savesearcher.output.OutputHandle;
import net.daporkchop.savesearcher.output.csv.CSVOutputHandle;
import net.daporkchop.savesearcher.tileentity.TileEntitySpawner;
import net.daporkchop.savesearcher.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * @author DaPorkchop_
 */
public class Main implements Logging {
    private static final Map<String, Function<String[], SearchModule>> REGISTERED_MODULES = new HashMap<String, Function<String[], SearchModule>>() {
        {
            /*this.put("--avgheight", AvgHeightModule::new);
            this.put("--block", BlockModule::new);
            this.put("--blockinrange", BlockRangeModule::new);
            this.put("--doublechest", DoubleChestModule::new);
            this.put("--emptychunks", EmptyChunksModule::new);
            this.put("--invertblock", InverseBlockModule::new);
            this.put("--invertblockinrange", InverseBlockRangeModule::new);
            this.put("--netherchunks", NetherChunksModule::new);
            this.put("--spawner", SpawnerModule::new);*/
            this.put("--sign", SignModule::new);
        }
    };

    private static final Map<String, Function<File, OutputHandle>> REGISTERED_OUTPUTS = new HashMap<String, Function<File, OutputHandle>>() {
        {
            this.put("csv", CSVOutputHandle::new);
        }
    };

    public static void main(String... args) throws IOException {
        logger.enableANSI().setLogAmount(LogAmount.DEBUG);

        Thread.currentThread().setUncaughtExceptionHandler((thread, ex) -> {
            logger.alert(ex);
            System.exit(1);
        });

        if (!PNatives.ZLIB.isNative()) {
            throw new IllegalStateException("Native zlib couldn't be loaded! Only supported on x86_64-linux-gnu, x86-linux-gnu and x86_64-w64-mingw32");
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
                    .info("--input=<path>                               Sets the input world path (required)")
                    .info("--dim=<dimension id>                         Sets the dimension (world) id to scan. default=0")
                    .info("--verbose                                    Print status updates to console")
                    .info("--format=<format>                            Sets the format that the output data will be written in. default=csv")
                    .info("--output=<path>                              Set the root directory that output data will be written to. default=./scanresult/")
                    .info("")
                    .info("MODULES")
                    .info("--avgheight                                   Calculate and save the average terrain height of the world")
                    .info("--block,id=<id>(,meta=<meta>)                 Scan for a certain block id+meta, saving coordinates. Block ids should be in format 'minecraft:stone'. Meta must be 0-15, by default it is ignored.")
                    .info("--blockinrange,id=<id>(,meta=<meta>)          Scan for a certain block id+meta in a given vertical range, saving coordinates. Both min and max")
                    .info("              (,min=<min>)(,max=<max>)        values are inclusive. See --block. defaults: min=0, max=255")
                    .info("--doublechest                                 Scan for double chests, saving coordinates and whether or not they're trapped.")
                    .warn("                                                WARNING! Can cause significant slowdown!")
                    .info("--invertblock,id=<id>(,meta=<meta>)           Scans for chunks that do not contain any of a certain block id+meta, saving chunk coordinates. See --block.")
                    .info("--invertblockinrange,id=<id>(,meta=<meta>)    Scans for chunks that do not contain any of a certain block id+meta in a given vertical range, saving chunk coordinates. See --blockinrange.")
                    .info("                    (,min=<min>)(,max=<max>)")
                    .info("--netherchunks                                Scan for nether chunks that have somehow ended up in the overworld.")
                    .info("--emptychunks                                 Scan for empty (air-only) chunks.")
                    .info("--sign                                        Scan for sign blocks, saving coordinates and text.")
                    .info("--spawner(,<id>)                              Scan for spawner blocks, optionally filtering based on mob type and saving coordinates and entity type.");
            return;
        } else {
            logger.addFile(new File("savesearcher.log").getAbsoluteFile(), true, LogAmount.DEBUG)
                    .info("SaveSearcher v%s", Version.VERSION)
                    .info("Copyright (c) DaPorkchop_")
                    .info("https://github.com/DaMatrix/SaveSearcher")
                    .info("")
                    .info("Starting...");
        }

        File worldFile = null;
        File outDir = new File("scanresult");
        int dim = 0;
        boolean verbose = false;
        boolean overwrite = false;
        String formatName = "csv";
        Collection<SearchModule> modules = new ArrayDeque<>();
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
            }
            split = s.split(",");
            Function<String[], SearchModule> function = REGISTERED_MODULES.get(split[0]);
            if (function == null) {
                logger.error("Invalid module: %s", split[0]);
                System.exit(1);
            }
            SearchModule module = function.apply(s.replaceAll(split[0], "").replaceAll(split[0] + ",", "").split(","));
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

        PFiles.ensureDirectoryExists(outDir);
        if (outDir.listFiles().length != 0) {
            if (overwrite) {
                logger.warn("Deleting contents of \"%s\" as -o is enabled...", outDir.getAbsolutePath());
            } else {
                logger.error("Output directory \"%s\" is not empty!", outDir.getAbsolutePath())
                        .error("Use -o to forcibly delete existing files, or delete them manually.");
                System.exit(1);
            }
        }
        PFiles.rmContents(outDir);

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
        AtomicLong count = new AtomicLong(0L);
        Set<Vec2i> regionPositions = Collections.newSetFromMap(new ConcurrentHashMap<>());
        try (MinecraftSave save = new SaveBuilder()
                .setInitFunctions(new MinecraftSaveConfig()
                        .openOptions(new RegionOpenOptions().access(RegionFile.Access.READ_ONLY).mode(RegionFile.Mode.MMAP_FULL))
                        .tileEntityFactory(TileEntityRegistry.builder(TileEntityRegistry.defaultRegistry())
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

            WorldScanner scanner = new WorldScanner(world) {
                @Override
                public WorldScanner addProcessor(ChunkProcessor processor) {
                    if (processor instanceof NeighboringChunkProcessor) {
                        return super.addProcessor((NeighboringChunkProcessor) processor);
                    } else {
                        return super.addProcessor(processor);
                    }
                }
            };
            if (verbose) {
                scanner.addProcessor((current, estimatedTotal, column) -> {
                    if (regionPositions.add(new Vec2i(column.getX() >> 5, column.getZ() >> 5))) {
                        logger.debug("Processing region #%d (%d,%d), chunk %d/~%d (%.2f%%)", regionPositions.size(), column.getX() >> 5, column.getZ() >> 5, current, estimatedTotal, ((double) current / (double) estimatedTotal) * 100.0d);
                    }
                });
            }
            scanner.addProcessor((current, estimatedTotal, column) -> count.set(current));
            modules.forEach(scanner::addProcessor);
            scanner.run(true);
        }
        logger.info("Finished scan. Saving data...");
        /*try (PrintStream out = new PrintStream(new FileOutputStream(outFile), false, "UTF-8")) {
            JsonObject obj = new JsonObject();
            Map<String, Collection<SearchModule>> byName = new HashMap<>();
            modules.forEach(m -> byName.computeIfAbsent(m.getSaveName(), n -> new ArrayList<>()).add(m));
            byName.forEach((name, withName) -> {
                JsonArray array = new JsonArray();
                withName.forEach(m -> {
                    JsonObject object = new JsonObject();
                    object.addProperty("name", m.toString());
                    JsonObject subObject = new JsonObject();
                    object.add("data", subObject);
                    m.saveData(subObject);
                    array.add(object);
                });
                obj.add(name, array);
            });
            gson.toJson(obj, out);
        }*/
        time = System.currentTimeMillis() - time;
        logger.success("Done!").success(
                "Scanned %d chunks (across %d regions) in %dh:%dm:%ds",
                count.get() + 1,
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
}
