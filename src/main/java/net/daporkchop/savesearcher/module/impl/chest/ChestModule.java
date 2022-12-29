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
import net.daporkchop.lib.minecraft.registry.IDRegistry;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.Section;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.module.merging.AbstractChunkSectionSearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
public abstract class ChestModule<T extends ChestModule.ChestData> extends AbstractChunkSectionSearchModule<T> {
    public static SearchModule find(@NonNull String[] args) {
        boolean doublechest = false;
        boolean above = false;

        for (String s : args) {
            String[] split = s.split("=");
            if (split.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
            switch (split[0]) {
                case "above":
                    above = Boolean.parseBoolean(split[1]);
                    break;
                case "double":
                    doublechest = Boolean.parseBoolean(split[1]);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
        }

        return doublechest
                ? above ? new DoubleChestModule.WithAbove() : new DoubleChestModule.Regular()
                : above ? new WithAbove() : new Regular();
    }

    private int chestId;
    private int trappedChestId;

    @Override
    public void init(@NonNull World world, @NonNull OutputHandle handle) {
        super.init(world, handle);

        this.chestId = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(new ResourceLocation("minecraft:chest"));
        this.trappedChestId = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(new ResourceLocation("minecraft:trapped_chest"));
    }

    @Override
    protected void processChunkSection(@NonNull Chunk chunk, @NonNull Section section) {
        final int chestId = this.chestId;
        final int trappedChestId = this.trappedChestId;

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    int id = section.getBlockId(x, y, z);
                    if (id == chestId || id == trappedChestId) {
                        this.handle.accept(this.createData(chunk.minX() + x, (section.getY() << 4) + y, chunk.minZ() + z, id == trappedChestId, chunk));
                    }
                }
            }
        }
    }

    protected abstract T createData(int x, int y, int z, boolean trapped, Chunk chunk);

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
    protected static class ChestData {
        public final int x;
        public final int y;
        public final int z;
        public final boolean trapped;
    }

    protected static class ChestDataWithAbove extends ChestData {
        public final ResourceLocation aboveId;
        public final int aboveMeta;

        public ChestDataWithAbove(int x, int y, int z, boolean trapped, ResourceLocation aboveId, int aboveMeta) {
            super(x, y, z, trapped);

            this.aboveId = aboveId;
            this.aboveMeta = aboveMeta;
        }
    }

    protected static final class Regular extends ChestModule<ChestData> {
        @Override
        protected ChestData createData(int x, int y, int z, boolean trapped, Chunk chunk) {
            return new ChestData(x, y, z, trapped);
        }

        @Override
        public String toString() {
            return "Chests";
        }
    }

    protected static final class WithAbove extends ChestModule<ChestDataWithAbove> {
        private IDRegistry blocksRegistry;

        @Override
        public void init(@NonNull World world, @NonNull OutputHandle handle) {
            super.init(world, handle);

            this.blocksRegistry = world.getSave().registry(new ResourceLocation("minecraft:blocks"));
        }

        @Override
        protected ChestDataWithAbove createData(int x, int y, int z, boolean trapped, Chunk chunk) {
            ResourceLocation aboveId = null;
            int aboveMeta = -1;

            if (y + 1 < chunk.maxY()) {
                aboveId = this.blocksRegistry.lookup(chunk.getBlockId(x & 0xF, y + 1, z & 0xF));
                aboveMeta = chunk.getBlockMeta(x & 0xF, y + 1, z & 0xF);
            }

            return new ChestDataWithAbove(x, y, z, trapped, aboveId, aboveMeta);
        }

        @Override
        public String toString() {
            return "Chests (w. block above)";
        }
    }
}
