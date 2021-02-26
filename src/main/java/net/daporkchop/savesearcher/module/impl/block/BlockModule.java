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

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.math.vector.i.Vec3i;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.Section;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class BlockModule extends AbstractSearchModule<Vec3i> {
    public static SearchModule find(@NonNull String[] args) {
        ResourceLocation id = null;
        int meta = -1;
        int min = 0;
        int max = 255;
        boolean invert = false;
        boolean chunkinvert = false;

        for (String s : args) {
            if (s.isEmpty()) {
                continue;
            }
            String[] split = s.split("=");
            if (split.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
            switch (split[0]) {
                case "id":
                    id = new ResourceLocation(split[1]);
                    break;
                case "meta":
                    meta = Integer.parseInt(split[1]);
                    if (meta > 15 || meta < 0) {
                        throw new IllegalArgumentException(String.format("Invalid meta: %d (must be in range 0-15)", meta));
                    }
                    break;
                case "min":
                case "minY":
                    min = Integer.parseInt(split[1]);
                    break;
                case "max":
                case "maxY":
                    max = Integer.parseInt(split[1]);
                    break;
                case "invert":
                    if (chunkinvert) {
                        throw new IllegalArgumentException("invert and chunkinvert cannot be used together!");
                    }
                    invert = true;
                    break;
                case "chunkinvert":
                    if (invert) {
                        throw new IllegalArgumentException("invert and chunkinvert cannot be used together!");
                    }
                    chunkinvert = true;
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
        }

        if (id == null) {
            throw new IllegalArgumentException("No id given!");
        } else if (min > max) {
            throw new IllegalArgumentException(String.format("Min Y must be less than or equal to max Y! (min=%d, max=%d)", min, max));
        } else if (min == 0 && max == 255) {
            return chunkinvert
                    ? new ChunkInverseBlockModule(id, meta)
                    : invert ? new InverseBlockModule(id, meta) : new BlockModule(id, meta);
        } else {
            return chunkinvert
                    ? new ChunkInverseBlockRangeModule(id, meta, min, max)
                    : invert ? new InverseBlockRangeModule(id, meta, min, max) : new BlockRangeModule(id, meta, min, max);
        }
    }

    protected final ResourceLocation searchName;
    protected final int meta;
    protected int id;

    @Override
    public void init(@NonNull World world, @NonNull OutputHandle handle) {
        super.init(world, handle);

        if ((this.id = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(this.searchName)) == -1) {
            throw new IllegalArgumentException(String.format("Invalid block id: %s", this.searchName.toString()));
        }
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk, @NonNull OutputHandle handle) {
        final int id = this.id;
        final int meta = this.meta;

        for (int sectionY = 0; sectionY < 16; sectionY++) {
            Section section = chunk.section(sectionY);
            if (section == null) {
                continue;
            }
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        if (section.getBlockId(x, y, z) == id && (meta < 0 || section.getBlockMeta(x, y, z) == meta)) {
                            handle.accept(new Vec3i(chunk.minX() + x, (section.getY() << 4) + y, chunk.minZ() + z));
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        if (this.meta == -1) {
            return String.format("Block (id=%s)", this.searchName);
        } else {
            return String.format("Block (id=%s, meta=%d)", this.searchName, this.meta);
        }
    }

    @Override
    public int hashCode() {
        //id is only computed later and can change dynamically, so we don't want to include it in the hash code
        return this.searchName.hashCode() * 31 + this.meta;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj.getClass() == BlockModule.class) {
            //don't do instanceof check, since we only want to check if the modules are exactly identical
            BlockModule other = (BlockModule) obj;
            return this.searchName.equals(other.searchName) && this.meta == other.meta;
        } else {
            return false;
        }
    }
}
