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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.daporkchop.lib.http.Http;
import net.daporkchop.lib.logging.LogAmount;
import net.daporkchop.lib.logging.Logging;
import net.daporkchop.lib.math.vector.i.Vec2i;
import net.daporkchop.lib.minecraft.region.WorldScanner;
import net.daporkchop.lib.minecraft.world.MinecraftSave;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.lib.minecraft.world.format.anvil.AnvilSaveFormat;
import net.daporkchop.lib.minecraft.world.impl.SaveBuilder;
import net.daporkchop.savesearcher.module.AvgHeightModule;
import net.daporkchop.savesearcher.module.DoubleChestModule;
import net.daporkchop.savesearcher.module.EmptyChunksModule;
import net.daporkchop.savesearcher.module.JourneymapModule;
import net.daporkchop.savesearcher.module.NetherChunksModule;
import net.daporkchop.savesearcher.module.SignModule;
import net.daporkchop.savesearcher.module.block.BlockModule;
import net.daporkchop.savesearcher.module.block.BlockRangeModule;
import net.daporkchop.savesearcher.module.block.InverseBlockModule;
import net.daporkchop.savesearcher.module.block.InverseBlockRangeModule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
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
    private static final Map<String, Function<String[], SearchModule>> registeredModules = new HashMap<>();

    static {
        registeredModules.put("--avgheight", AvgHeightModule::new);
        registeredModules.put("--block", BlockModule::new);
        registeredModules.put("--blockinrange", BlockRangeModule::new);
        registeredModules.put("--doublechest", DoubleChestModule::new);
        registeredModules.put("--emptychunks", EmptyChunksModule::new);
        registeredModules.put("--invertblock", InverseBlockModule::new);
        registeredModules.put("--invertblockinrange", InverseBlockRangeModule::new);
        registeredModules.put("--journeymap", JourneymapModule::new);
        registeredModules.put("--netherchunks", NetherChunksModule::new);
        registeredModules.put("--sign", SignModule::new);
    }

    public static void main(String... args) throws IOException {
        logger.enableANSI().setLogAmount(LogAmount.DEBUG);

        Thread.currentThread().setUncaughtExceptionHandler((thread, ex) -> {
            logger.alert(ex);
            System.exit(1);
        });

        String versionName;
        {
            JsonParser parser = new JsonParser();
            JsonObject local;
            try (Reader reader = new InputStreamReader(Main.class.getResourceAsStream("/version.json"))) {
                local = parser.parse(reader).getAsJsonObject();
            }
            try {
                JsonObject remote = parser.parse(Http.getString("https://raw.githubusercontent.com/DaMatrix/SaveSearcher/master/src/main/resources/version.json")).getAsJsonObject();
                int localVersion = Integer.parseInt(local.get("versionNew").getAsString().replaceAll("_", ""));
                int remoteVersion = Integer.parseInt(remote.get("versionNew").getAsString().replaceAll("_", ""));
                if (localVersion < remoteVersion) {
                    logger.alert(
                            "Outdated version! You're still on %s, but the latest version is %s.\nDownload the latest version from https://github.com/DaMatrix/SaveSearcher.\n\nScanner will start in 5 seconds...",
                            local.get("nameNew").getAsString().replaceAll(" ", ""),
                            remote.get("nameNew").getAsString().replaceAll(" ", "")
                    );
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
                logger.warn("Version check failed, ignoring");
            }
            versionName = local.get("nameNew").getAsString().replaceAll(" ", "");
        }
        File worldFile = null;
        File outFile = new File(".", "scanresult.json");
        int dim = 0;
        boolean verbose = false;
        boolean prettyPrintJson = false;
        Collection<SearchModule> modules = new ArrayDeque<>();
        if (args.length == 0
                || contains(args, "-h")
                || contains(args, "--help")
                || contains(args, "--h")
                || contains(args, "-help")) {
            logger.info("SaveSearcher v%s", versionName)
                    .info("Copyright (c) DaPorkchop_")
                    .info("https://github.com/DaMatrix/SaveSearcher")
                    .info("")
                    .info("--input=<path>                               Sets the input world path (required)")
                    .info("--dim=<dimension id>                         Sets the dimension (world) id to scan. default=0")
                    .info("--verbose                                    Print status updates to console")
                    .info("--prettyPrintJson                            Makes the output json data be formatted")
                    .info("--output=<path>                              Set the file that output data will be written to. default=./scanresult.json")
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
                    .info("--journeymap,root=<path>                      Generate waypoint files for JourneyMap in the given output directory. Waypoints for each module will be placed in their own subdirectory.")
                    .info("--netherchunks                                Scan for nether chunks that have somehow ended up in the overworld.")
                    .info("--emptychunks                                 Scan for empty (air-only) chunks.")
                    .info("--sign                                        Scan for sign blocks, saving coordinates and text.");
            return;
        } else {
            logger.addFile(new File(String.format("savesearcher-%d.log", System.currentTimeMillis())).getAbsoluteFile(), true, LogAmount.DEBUG)
                    .info("SaveSearcher v%s", versionName)
                    .info("Copyright (c) DaPorkchop_")
                    .info("https://github.com/DaMatrix/SaveSearcher")
                    .info("")
                    .info("Starting...");
        }
        for (String s : args) {
            if (s.isEmpty()) {
                continue;
            }
            String[] split = s.split("=");
            switch (split[0]) {
                case "--input":
                case "-i": {
                    worldFile = new File(split[1]);
                }
                continue;
                case "--output":
                case "-o": {
                    outFile = new File(split[1]);
                }
                continue;
                case "--dim": {
                    if (true) throw new UnsupportedOperationException("--dim option is currently unsupported!");
                    dim = Integer.parseInt(split[1]);
                }
                continue;
                case "--verbose":
                case "-v": {
                    verbose = true;
                }
                continue;
                case "--prettyPrintJson": {
                    prettyPrintJson = true;
                }
                continue;
            }
            split = s.split(",");
            Function<String[], SearchModule> function = registeredModules.get(split[0]);
            if (function == null) {
                throw new IllegalArgumentException(String.format("Invalid module: %s", split[0]));
            }
            SearchModule module = function.apply(s.replaceAll(split[0], "").replaceAll(split[0] + ",", "").split(","));
            if (modules.contains(module))   {
                logger.warn("Duplicate argument: \"%s\"!", s);
            } else {
                modules.add(module);
            }
        }
        if (worldFile == null) {
            throw new IllegalArgumentException("World path not set!");
        } else if (modules.isEmpty()) {
            throw new IllegalArgumentException("No modules enabled!");
        }

        Gson gson;
        {
            GsonBuilder builder = new GsonBuilder();
            if (prettyPrintJson) {
                builder.setPrettyPrinting();
            }
            gson = builder.create();
        }

        logger.info("Beginning scan of world %s with %d modules enabled.", worldFile.getAbsolutePath(), modules.size())
                .info("Modules:");
        modules.forEach(m -> logger.info("  %s", m.toString()));

        long time = System.currentTimeMillis();
        AtomicLong count = new AtomicLong(0L);
        Set<Vec2i> regionPositions = Collections.newSetFromMap(new ConcurrentHashMap<>());
        try (MinecraftSave save = new SaveBuilder().setFormat(new AnvilSaveFormat(worldFile)).build()) {
            World world = save.getWorld(dim);
            if (world == null) {
                throw new IllegalArgumentException(String.format("Invalid world: %d", dim));
            }
            modules.forEach(m -> m.init(world));
            WorldScanner scanner = new WorldScanner(world) {
                @Override
                public WorldScanner addProcessor(ColumnProcessor processor) {
                    if (processor instanceof ColumnProcessorNeighboring) {
                        return super.addProcessor((ColumnProcessorNeighboring) processor);
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
            //scanner.requireNeighboring();
            scanner.run(true);

            modules.forEach(m -> m.beforeExit(modules, gson, world));
        }
        logger.info("Finished scan. Saving data...");
        try (PrintStream out = new PrintStream(new FileOutputStream(outFile), false, "UTF-8")) {
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
        }
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
