/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2018-2018 DaPorkchop_ and contributors
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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.daporkchop.lib.binary.UTF8;
import net.daporkchop.lib.http.SimpleHTTP;
import net.daporkchop.lib.minecraft.region.WorldScanner;
import net.daporkchop.lib.minecraft.world.MinecraftSave;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.lib.minecraft.world.format.anvil.AnvilSaveFormat;
import net.daporkchop.lib.minecraft.world.impl.SaveBuilder;
import net.daporkchop.savesearcher.module.AvgHeightModule;
import net.daporkchop.savesearcher.module.BlockModule;
import net.daporkchop.savesearcher.module.DoubleChestModule;
import net.daporkchop.savesearcher.module.SignModule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * @author DaPorkchop_
 */
public class Main {
    private static final Map<String, Function<String[], SearchModule>> registeredModules = new Hashtable<>();

    static {
        registeredModules.put("--block", BlockModule::new);
        registeredModules.put("--sign", SignModule::new);
        registeredModules.put("--doublechest", DoubleChestModule::new);
        registeredModules.put("--avgheight", AvgHeightModule::new);
    }

    public static void main(String... args) throws IOException {
        String versionName;
        {
            JsonParser parser = new JsonParser();
            JsonObject local;
            try (Reader reader = new InputStreamReader(Main.class.getResourceAsStream("/version.json"))) {
                local = parser.parse(reader).getAsJsonObject();
            }
            try {
                JsonObject remote = parser.parse(SimpleHTTP.getString("https://raw.githubusercontent.com/DaMatrix/SaveSearcher/master/src/main/resources/version.json")).getAsJsonObject();
                if (local.get("version").getAsInt() < remote.get("version").getAsInt()) {
                    System.out.printf("Outdated version! You're still on %s, but the latest version is %s.\n", local.get("name").getAsString(), remote.has("name") ? remote.get("name").getAsString() : "null");
                    System.out.println("Download the latest version from https://github.com/DaMatrix/SaveSearcher");
                    System.out.println("Scanner will start in 5 seconds...");
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (Throwable t)   {
                t.printStackTrace();
                System.out.println();
                System.out.println("Version check failed, ignoring");
            }
            versionName = local.get("name").getAsString();
        }
        File worldFile = null;
        File outFile = new File(".", "scanresult.json");
        int dim = 0;
        boolean verbose = false;
        boolean prettyPrintJson = false;
        Collection<SearchModule> modules = new ArrayDeque<>();
        System.out.printf("SaveSearcher v%s\n", versionName);
        System.out.println("Copyright (c) DaPorkchop_");
        System.out.println("https://github.com/DaMatrix/SaveSearcher");
        if (args.length == 0 || contains(args, "-h") || contains(args, "--help"))   {
            System.out.println();
            System.out.println("--input=<path>                   Sets the input world path (required)");
            System.out.println("--dim=<dimension id>             Sets the dimension (world) id to scan. default=0");
            System.out.println("--verbose                        Print status updates to console");
            System.out.println("--prettyPrintJson                Makes the output json data be formatted");
            System.out.println("--output=<path>                  Set the file that output data will be written to. default=./scanresult.json");
            System.out.println();
            System.out.println("MODULES");
            System.out.println("--block,id=<id>(,meta=<meta>)    Scan for a certain block id+meta, saving coordinates. Block ids should be in format 'minecraft:stone'. Meta must be 0-15, by default it is ignored.");
            System.out.println("--sign                           Scan for sign blocks, saving coordinates and text.");
            System.out.println("--doublechest                    Scan for double chests, saving coordinates and whether or not they're trapped.");
            System.out.println("                                 WARNING! Can cause significant slowdown!");
            System.out.println("--avgheight                      Calculate and save the average terrain height of the world");
            return;
        } else {
            System.out.println("Starting...");
        }
        for (String s : args)   {
            String[] split = s.split("=");
            switch (split[0])   {
                case "--input":
                case "-i":   {
                    worldFile = new File(split[1]);
                }
                continue;
                case "--output":
                case "-o":   {
                    outFile = new File(split[1]);
                }
                continue;
                case "--dim": {
                    dim = Integer.parseInt(split[1]);
                }
                continue;
                case "--verbose":
                case "-v":  {
                    verbose = true;
                }
                continue;
                case "--prettyPrintJson":{
                    prettyPrintJson = true;
                }
                continue;
            }
            split = s.split(",");
            Function<String[], SearchModule> function = registeredModules.get(split[0]);
            if (function == null)   {
                throw new IllegalArgumentException(String.format("Invalid module: %s", split[0]));
            }
            modules.add(function.apply(s.replaceAll(split[0], "").replaceAll(split[0] + ",", "").split(",")));
        }
        if (worldFile == null)  {
            throw new IllegalArgumentException("World path not set!");
        } else if (modules.isEmpty())   {
            throw new IllegalArgumentException("No modules enabled!");
        }
        long time = System.currentTimeMillis();
        AtomicLong count = new AtomicLong(0L);
        try (MinecraftSave save = new SaveBuilder().setFormat(new AnvilSaveFormat(worldFile)).build()) {
            World world = save.getWorld(dim);
            if (world == null)  {
                throw new IllegalArgumentException(String.format("Invalid world: %d", dim));
            }
            modules.forEach(m -> m.init(world));
            WorldScanner scanner = new WorldScanner(world)  {
                @Override
                public WorldScanner addProcessor(ColumnProcessor processor) {
                    if (processor instanceof ColumnProcessorNeighboring)    {
                        return super.addProcessor((ColumnProcessorNeighboring) processor);
                    } else {
                        return super.addProcessor(processor);
                    }
                }
            };
            if (verbose)    {
                scanner.addProcessor((curr, remaining, col) -> {
                    count.set(curr);
                    if ((col.getX() & 0x1F) == 31 && (col.getZ() & 0x1F) == 31)    {
                        System.out.printf("Processing region (%d,%d), chunk %d/~%d (%.2f%%)\n", col.getX() >> 5, col.getZ() >> 5, curr, remaining, ((double) curr / (double) remaining) * 100.0d);
                    }
                });
            }
            modules.forEach(scanner::addProcessor);
            //scanner.requireNeighboring();
            scanner.run(true);
        }
        System.out.println("Finished scan. Saving data...");
        try (OutputStream os = new FileOutputStream(outFile)) {
            JsonObject object = new JsonObject();
            modules.forEach(m -> m.saveData(object));
            GsonBuilder builder = new GsonBuilder();
            if (prettyPrintJson)    {
                builder.setPrettyPrinting();
            }
            os.write(builder.create().toJson(object).getBytes(UTF8.utf8));
        }
        System.out.println("Done!");
        time = System.currentTimeMillis() - time;
        System.out.printf(
                "Scanned %d chunks in %dh:%dm:%ds\n",
                count.get(),
                time / (1000L * 60L * 60L),
                time / (1000L * 60L) % 60,
                time / (1000L) % 60
        );
    }

    private static boolean contains(String[] arr, String s) {
        for (String s1 : arr)   {
            if (s1.equalsIgnoreCase(s))  {
                return true;
            }
        }

        return false;
    }
}
