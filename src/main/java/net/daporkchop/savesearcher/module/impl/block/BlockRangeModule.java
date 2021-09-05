/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2018-2021 DaPorkchop_
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

package net.daporkchop.savesearcher.module.impl.block;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.math.vector.i.Vec3i;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
final class BlockRangeModule extends AbstractSearchModule<Vec3i> {
    protected final ResourceLocation searchName;
    protected final int meta;
    protected final int minY;
    protected final int maxY;

    @EqualsAndHashCode.Exclude
    protected int id;

    @Override
    public void init(@NonNull World world, @NonNull OutputHandle handle) {
        super.init(world, handle);

        if ((this.id = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(this.searchName)) == -1) {
            throw new IllegalArgumentException(String.format("Invalid block id: %s", this.searchName));
        }
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        final int id = this.id;
        final int meta = this.meta;
        final int maxY = this.maxY;
        final int minY = this.minY;

        for (int y = minY; y <= maxY; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    if (chunk.getBlockId(x, y, z) == id && (meta < 0 || chunk.getBlockMeta(x, y, z) == meta)) {
                        this.handle.accept(new Vec3i(chunk.minX() + x, y, chunk.minZ() + z));
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        if (this.meta == -1) {
            return String.format("Block - Ranged (id=%s, min=%d, max=%d)", this.searchName, this.minY, this.maxY);
        } else {
            return String.format("Block - Ranged (id=%s, meta=%d, min=%d, max=%d)", this.searchName, this.meta, this.minY, this.maxY);
        }
    }
}
