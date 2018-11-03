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

package net.daporkchop.savesearcher.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Column;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.SearchModule;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author DaPorkchop_
 */
public class BlockModule implements SearchModule {
    private final JsonArray values = new JsonArray();
    private ResourceLocation searchName;
    private int meta = -1;
    private int id;

    public BlockModule(String[] args) {
        for (String s : args) {
            if (s.isEmpty()) {
                continue;
            }
            String[] split = s.split("=");
            if (split.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
            switch (split[0]) {
                case "id": {
                    this.searchName = new ResourceLocation(split[1]);
                }
                break;
                case "meta": {
                    this.meta = Integer.parseInt(split[1]);
                    if (this.meta > 15 || this.meta < 0) {
                        throw new IllegalArgumentException(String.format("Invalid meta: %d (must be in range 0-15)", this.meta));
                    }
                }
                break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
        }
        if (this.searchName == null) {
            throw new IllegalArgumentException("No id given!");
        }
    }

    @Override
    public void init(World world) {
        this.id = world.getSave().getRegistry(new ResourceLocation("minecraft:blocks")).getId(this.searchName);
    }

    @Override
    public void saveData(JsonObject object) {
        object.add(String.format("block_%s_%d", this.searchName.toString(), this.meta), this.values);
    }

    @Override
    public void handle(long current, long estimatedTotal, Column column) {
        for (int x = 15; x >= 0; x--) {
            for (int z = 15; z >= 0; z--) {
                for (int y = 255; y >= 0; y--) {
                    if (column.getBlockId(x, y, z) == this.id && (this.meta == -1 || column.getBlockMeta(x, y, z) == this.meta)) {
                        JsonObject object = new JsonObject();
                        object.addProperty("x", x);
                        object.addProperty("y", y);
                        object.addProperty("z", z);
                        synchronized (this.values)   {
                            this.values.add(object);
                        }
                    }
                }
            }
        }
    }
}
