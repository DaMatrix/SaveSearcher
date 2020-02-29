/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2018-2020 DaPorkchop_
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
