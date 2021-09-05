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

package net.daporkchop.savesearcher.module.impl;

import lombok.NonNull;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.Section;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.PositionDataXZ;

/**
 * @author DaPorkchop_
 */
public final class EmptyChunksModule extends AbstractSearchModule<PositionDataXZ> {
    public EmptyChunksModule(String[] args) {
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        for (int chunkY = 0; chunkY < 15; chunkY++) { //go from bottom section to top as it's more likely to find blocks on the bottom
            final Section section = chunk.section(chunkY);
            if (section == null) {
                continue;
            }
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        if (section.getBlockId(x, y, z) != 0) {
                            return;
                        }
                    }
                }
            }
        }
        this.handle.accept(new PositionDataXZ(chunk.pos()));
    }

    @Override
    public String toString() {
        return "Empty Chunks";
    }

    @Override
    public int hashCode() {
        return EmptyChunksModule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EmptyChunksModule;
    }
}
