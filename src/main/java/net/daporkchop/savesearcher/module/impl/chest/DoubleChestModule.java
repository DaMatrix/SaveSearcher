/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2018-2022 DaPorkchop_
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

package net.daporkchop.savesearcher.module.impl.chest;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.minecraft.region.util.NeighboringChunkProcessor;
import net.daporkchop.lib.minecraft.registry.IDRegistry;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.util.BlockAccess;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
abstract class DoubleChestModule<T extends DoubleChestModule.DoubleChestData> extends AbstractSearchModule<T> implements NeighboringChunkProcessor {
    private int chestId;
    private int trappedChestId;

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
    protected void processChunk(@NonNull Chunk chunk) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handle(long current, long estimatedTotal, Chunk chunk, BlockAccess access) {
        final int chestId = this.chestId;
        final int trappedChestId = this.trappedChestId;
        final int x = chunk.getX() << 4;
        final int z = chunk.getZ() << 4;

        for (int xx = 15; xx >= 0; xx--) {
            for (int zz = 15 - (xx & 1); zz >= 0; zz -= 2) {
                for (int y = 255; y >= 0; y--) {
                    int id = access.getBlockId(x + xx, y, z + zz);
                    if (id == chestId || id == trappedChestId) {
                        if (access.getBlockId(x + xx + 1, y, z + zz) == id) {
                            this.handle.accept(this.createData(x + xx, y, z + zz, x + xx + 1, y, z + zz, id == trappedChestId, access));
                        } else if (access.getBlockId(x + xx, y, z + zz + 1) == id) {
                            this.handle.accept(this.createData(x + xx, y, z + zz, x + xx, y, z + zz + 1, id == trappedChestId, access));
                        }
                    }
                }
            }
        }
    }

    protected abstract T createData(int x0, int y0, int z0, int x1, int y1, int z1, boolean trapped, BlockAccess access);

    @Override
    public abstract String toString();

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == this.getClass();
    }

    @RequiredArgsConstructor
    protected static class DoubleChestData {
        public final int x0;
        public final int y0;
        public final int z0;
        public final int x1;
        public final int y1;
        public final int z1;
        public final boolean trapped;
    }

    protected static class DoubleChestDataWithAbove extends DoubleChestData {
        public final ResourceLocation aboveId0;
        public final int aboveMeta0;
        public final ResourceLocation aboveId1;
        public final int aboveMeta1;

        public DoubleChestDataWithAbove(int x0, int y0, int z0, int x1, int y1, int z1, boolean trapped, ResourceLocation aboveId0, int aboveMeta0, ResourceLocation aboveId1, int aboveMeta1) {
            super(x0, y0, z0, x1, y1, z1, trapped);

            this.aboveId0 = aboveId0;
            this.aboveMeta0 = aboveMeta0;
            this.aboveId1 = aboveId1;
            this.aboveMeta1 = aboveMeta1;
        }
    }

    protected static final class Regular extends DoubleChestModule<DoubleChestData> {
        @Override
        protected DoubleChestData createData(int x0, int y0, int z0, int x1, int y1, int z1, boolean trapped, BlockAccess access) {
            return new DoubleChestData(x0, y0, z0, x1, y1, z1, trapped);
        }

        @Override
        public String toString() {
            return "Double Chests";
        }
    }

    protected static final class WithAbove extends DoubleChestModule<DoubleChestDataWithAbove> {
        private IDRegistry blocksRegistry;

        @Override
        public void init(@NonNull World world, @NonNull OutputHandle handle) {
            super.init(world, handle);

            this.blocksRegistry = world.getSave().registry(new ResourceLocation("minecraft:blocks"));
        }

        @Override
        protected DoubleChestDataWithAbove createData(int x0, int y0, int z0, int x1, int y1, int z1, boolean trapped, BlockAccess access) {
            ResourceLocation aboveId0 = null;
            int aboveMeta0 = -1;
            ResourceLocation aboveId1 = null;
            int aboveMeta1 = -1;

            if (y0 + 1 < access.maxY()) {
                aboveId0 = this.blocksRegistry.lookup(access.getBlockId(x0, y0 + 1, z0));
                aboveMeta0 = access.getBlockMeta(x0, y0 + 1, z0);
            }
            if (y1 + 1 < access.maxY()) {
                aboveId1 = this.blocksRegistry.lookup(access.getBlockId(x1, y1 + 1, z1));
                aboveMeta1 = access.getBlockMeta(x1, y1 + 1, z1);
            }

            return new DoubleChestDataWithAbove(x0, y0, z0, x1, y1, z1, trapped, aboveId0, aboveMeta0, aboveId1, aboveMeta1);
        }

        @Override
        public String toString() {
            return "Double Chests (w. block above)";
        }
    }
}
