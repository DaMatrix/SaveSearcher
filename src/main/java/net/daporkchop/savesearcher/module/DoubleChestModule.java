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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.daporkchop.lib.math.vector.i.Vec3i;
import net.daporkchop.lib.minecraft.region.WorldScanner;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.SearchModule;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author DaPorkchop_
 */
public class DoubleChestModule extends SearchModule.BasePosSearchModule implements WorldScanner.ColumnProcessorNeighboring {
    private int chestId;
    private int trappedChestId;

    public DoubleChestModule(String[] args) {
    }

    @Override
    public void init(World world) {
        this.chestId = world.getSave().getRegistry(new ResourceLocation("minecraft:blocks")).getId(new ResourceLocation("minecraft:chest"));
        this.trappedChestId = world.getSave().getRegistry(new ResourceLocation("minecraft:blocks")).getId(new ResourceLocation("minecraft:trapped_chest"));
    }

    @Override
    public void handle(long current, long estimatedTotal, World world, int x, int z) {
        for (int xx = 15; xx >= 0; xx--) {
            for (int zz = (xx & 1) == 0 ? 15 : 14; zz >= 0; zz -= 2) {
                for (int y = 255; y >= 0; y--) {
                    int id = world.getBlockId(x + xx, y, z + zz);
                    if (id == this.chestId)  {
                        if (world.getBlockId(x + xx + 1, y, z + zz) == this.chestId
                                || world.getBlockId(x + xx, y, z + zz + 1) == this.chestId) {
                            this.add(x + xx, y, z + zz, false);
                        }
                    } else if (id == this.trappedChestId)    {
                        if (world.getBlockId(x + xx + 1, y, z + zz) == this.trappedChestId
                                || world.getBlockId(x + xx, y, z + zz + 1) == this.trappedChestId) {
                            this.add(x + xx, y, z + zz, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected JsonObject getObject(int x, int y, int z, Object... args) {
        JsonObject object = super.getObject(x, y, z, args);
        object.addProperty("trapped", (boolean) args[0]);
        return object;
    }

    @Override
    public String toString() {
        return "Double Chests";
    }

    @Override
    public String getSaveName() {
        return "double_chest";
    }
}
