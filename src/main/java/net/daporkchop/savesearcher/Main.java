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
import net.daporkchop.lib.binary.UTF8;
import net.daporkchop.lib.minecraft.region.WorldScanner;
import net.daporkchop.lib.minecraft.world.MinecraftSave;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.lib.minecraft.world.format.anvil.AnvilSaveFormat;
import net.daporkchop.lib.minecraft.world.impl.SaveBuilder;
import net.daporkchop.savesearcher.module.BlockModule;
import net.daporkchop.savesearcher.module.SignModule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;

/**
 * @author DaPorkchop_
 */
public class Main {
    private static final Map<String, Function<String[], SearchModule>> registeredModules = new Hashtable<>();

    static {
        registeredModules.put("--block", BlockModule::new);
        registeredModules.put("--sign", SignModule::new);
    }

    public static void main(String... args) throws IOException {
        File worldFile = null;
        File outFile = new File(".", "scanresult.json");
        int dim = 0;
        boolean verbose = false;
        boolean prettyPrintJson = false;
        Collection<SearchModule> modules = new ArrayDeque<>();
        if (args.length == 0 || contains(args, "-h") || contains(args, "--help"))   {
            System.out.println("SaveSearcher v0.0.1");
            System.out.println();
            System.out.println("--input=<path>                   Sets the input world path (required)");
            System.out.println("--dim=<dimension id>             Sets the dimension (world) id to scan. default=0");
            System.out.println("--verbose                        Print status updates to console");
            System.out.println("--prettyPrintJson                Makes the output json data be formatted");
            System.out.println("--output=<path>                  Set the file that output data will be written to. default=./scanresult.json");
            return;
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
                    if ((col.getX() & 0x1F) == 31 && (col.getZ() & 0x1F) == 31)    {
                        System.out.printf("Processing region (%d,%d), on chunk %d/~%d\n", col.getX() >> 5, col.getZ() >> 5, curr, remaining);
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
