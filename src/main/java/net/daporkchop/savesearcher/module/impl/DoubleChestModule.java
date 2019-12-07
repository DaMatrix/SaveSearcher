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

package net.daporkchop.savesearcher.module.impl;

import com.google.gson.JsonObject;
import lombok.NonNull;
import net.daporkchop.lib.minecraft.region.util.NeighboringChunkProcessor;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.util.BlockAccess;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.PositionData;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
public final class DoubleChestModule extends AbstractSearchModule<DoubleChestModule.DoubleChestData> implements NeighboringChunkProcessor {
    private int chestId;
    private int trappedChestId;

    public DoubleChestModule(String[] args) {
    }

    @Override
    public void init(@NonNull World world, @NonNull OutputHandle handle) {
        super.init(world, handle);

        this.chestId = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(new ResourceLocation("minecraft:chest"));
        this.trappedChestId = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(new ResourceLocation("minecraft:trapped_chest"));
    }

    @Override
    public void handle(long l, long l1, Chunk chunk) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk, @NonNull OutputHandle handle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handle(long current, long estimatedTotal, Chunk chunk, BlockAccess access) {
        final int chestId = this.chestId;
        final int trappedChestId = this.trappedChestId;
        final int x = chunk.getX() << 4;
        final int z = chunk.getZ() << 4;

        for (int xx = 15; xx >= 0; xx--) {
            for (int zz = (xx & 1) == 0 ? 15 : 14; zz >= 0; zz -= 2) {
                for (int y = 255; y >= 0; y--) {
                    int id = access.getBlockId(x + xx, y, z + zz);
                    if (id == chestId)  {
                        if (access.getBlockId(x + xx + 1, y, z + zz) == chestId
                                || access.getBlockId(x + xx, y, z + zz + 1) == chestId) {
                            this.handle.accept(new DoubleChestData(x + xx, y, z + zz, false));
                        }
                    } else if (id == trappedChestId)    {
                        if (access.getBlockId(x + xx + 1, y, z + zz) == trappedChestId
                                || access.getBlockId(x + xx, y, z + zz + 1) == trappedChestId) {
                            this.handle.accept(new DoubleChestData(x + xx, y, z + zz, true));
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Double Chests";
    }

    @Override
    public int hashCode() {
        return DoubleChestModule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DoubleChestModule;
    }

    protected static final class DoubleChestData extends PositionData   {
        public final boolean trapped;

        public DoubleChestData(int x, int y, int z, boolean trapped) {
            super(x, y, z);

            this.trapped = trapped;
        }
    }
}
