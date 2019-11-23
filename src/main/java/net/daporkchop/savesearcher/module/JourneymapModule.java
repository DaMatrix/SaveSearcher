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

package net.daporkchop.savesearcher.module;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.daporkchop.lib.math.vector.i.Vec3i;
import net.daporkchop.lib.minecraft.world.Column;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.SearchModule;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author DaPorkchop_
 */
public class JourneymapModule implements SearchModule {
    protected File rootDir;

    public JourneymapModule(String[] args) {
        for (String s : args) {
            if (s.isEmpty()) {
                continue;
            }
            String[] split = s.split("=");
            if (split.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
            switch (split[0]) {
                case "root": {
                    this.rootDir = new File(split[1]);
                }
                break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
        }
        if (this.rootDir == null) {
            throw new IllegalArgumentException("No file given!");
        } else if ((!this.rootDir.exists() && !this.rootDir.mkdirs()) || !this.rootDir.isDirectory())  {
            throw new IllegalArgumentException(String.format("Invalid root directory: %s", this.rootDir.getAbsolutePath()));
        }
    }

    protected JourneymapModule() {
    }

    @Override
    public void init(World world) {
    }

    @Override
    public void saveData(JsonObject object, Gson gson) {
        object.addProperty("output", this.rootDir.getAbsolutePath());
    }

    @Override
    public void handle(long current, long estimatedTotal, Column column) {
    }

    @Override
    public void beforeExit(Collection<SearchModule> modules, Gson gson, World world) {
        System.out.println("Generating Journeymap waypoints...");
        System.out.printf("Deleting output directory %s ...\n", this.rootDir.getAbsolutePath());
        rmDir(this.rootDir);
        JsonArray dimensionsArray = new JsonArray();
        dimensionsArray.add(world.getId());
        AtomicInteger totalCount = new AtomicInteger(0);
        modules.forEach(module -> {
            Collection<Vec3i> locations = module.getLocations();
            if (locations != null && !locations.isEmpty()) {
                String name = module.toString();
                Color color = new Color(name.hashCode());
                System.out.printf("Generating waypoints for \"%s\"...\n", name);
                File outDir = new File(this.rootDir, name);
                if (!outDir.mkdirs())   {
                    throw new IllegalStateException(String.format("Couldn't create directory: %s", outDir.getAbsolutePath()));
                }
                AtomicInteger counter = new AtomicInteger(0);
                locations.parallelStream().forEach(loc -> { //parallel, there's a lot of wasted time on disk io and whatnot so it can't hurt
                    try {
                        totalCount.incrementAndGet();
                        int id = counter.getAndIncrement();
                        File file = new File(outDir, String.format("%s #%d_%d,%d,%d.json", name, id, loc.getX(), loc.getY(), loc.getZ()));
                        if (!file.createNewFile()) {
                            throw new IllegalStateException(String.format("Couldn't create file: %s", file.getAbsolutePath()));
                        }
                        try (OutputStream os = new FileOutputStream(file)) {
                            JsonObject object = new JsonObject();
                            object.addProperty("id", String.format("%s #%d_%d,%d,%d", name, id, loc.getX(), loc.getY(), loc.getZ()));
                            object.addProperty("name", String.format("%s #%d", name, id));
                            object.addProperty("icon", "waypoint-normal.png");
                            object.addProperty("x", loc.getX());
                            object.addProperty("y", loc.getY());
                            object.addProperty("z", loc.getZ());
                            object.addProperty("r", color.getRed());
                            object.addProperty("g", color.getGreen());
                            object.addProperty("b", color.getBlue());
                            object.addProperty("enable", true);
                            object.addProperty("type", "Normal");
                            object.addProperty("origin", "journeymap");
                            object.add("dimensions", dimensionsArray);
                            object.addProperty("persistent", true);
                            os.write(gson.toJson(object).getBytes(StandardCharsets.UTF_8));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
        System.out.printf("%d waypoints generated.\n", totalCount.get());
    }

    @Override
    public String toString() {
        return String.format("Journeymap Waypoints (output=%s)", this.rootDir.getAbsolutePath());
    }

    @Override
    public String getSaveName() {
        return "journeymap";
    }

    private static void rmDir(File file)    {
        rmDir(file, false);
    }

    private static void rmDir(File file, boolean sub)    {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                rmDir(f, true);
            }
            if (sub)    {
                file.delete();
            }
        } else {
            file.delete();
        }
    }
}
