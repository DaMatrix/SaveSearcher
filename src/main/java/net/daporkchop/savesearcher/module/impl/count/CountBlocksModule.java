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

package net.daporkchop.savesearcher.module.impl.count;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.math.vector.i.Vec2i;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.Section;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.PositionDataXZ;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class CountBlocksModule extends AbstractSearchModule<CountBlocksModule.CountData> {
    public static SearchModule find(@NonNull String[] args) {
        ResourceLocation id = null;
        int meta = -1;
        String type = null;

        for (String s : args) {
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
                case "type":
                    type = split[1];
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
        }

        if (type == null) {
            throw new IllegalArgumentException("No type given!");
        } else {
            switch (type) {
                case "block":
                    if (id == null) {
                        throw new IllegalArgumentException("No id given!");
                    }
                    return new CountBlocksModule(id, meta);
                case "tileentity":
                    if (meta != -1) {
                        throw new IllegalStateException("type=tileentity does not use a meta value!");
                    }
                    return new CountTileEntitiesModule(id);
                default:
                    throw new IllegalStateException(String.format("Unknown type: \"%s\"", type));
            }
        }
    }

    protected final ResourceLocation searchName;
    protected final int              meta;
    protected       int              id;

    @Override
    public void init(@NonNull World world, @NonNull OutputHandle handle) {
        super.init(world, handle);

        if ((this.id = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(this.searchName)) == -1) {
            throw new IllegalArgumentException(String.format("Invalid block id: %s", this.searchName.toString()));
        }
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        final int id = this.id;
        final int meta = this.meta;

        long count = 0L;
        for (int sectionY = 0; sectionY < 16; sectionY++) {
            Section section = chunk.section(sectionY);
            if (section == null) {
                if (id == 0 && meta == 0) {
                    count += 4096L;
                }
                continue;
            }
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        if (section.getBlockId(x, y, z) == id && (meta < 0 || section.getBlockMeta(x, y, z) == meta)) {
                            count++;
                        }
                    }
                }
            }
        }
        this.handle.accept(new CountData(chunk.pos(), count));
    }

    @Override
    public String toString() {
        if (this.meta == -1) {
            return String.format("Count - Block (id=%s)", this.searchName);
        } else {
            return String.format("Count - Block (id=%s, meta=%d)", this.searchName, this.meta);
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
        } else if (obj.getClass() == CountBlocksModule.class) {
            //don't do instanceof check, since we only want to check if the modules are exactly identical
            CountBlocksModule other = (CountBlocksModule) obj;
            return this.searchName.equals(other.searchName) && this.meta == other.meta;
        } else {
            return false;
        }
    }

    protected static final class CountData extends PositionDataXZ {
        public final long count;

        public CountData(Vec2i vec, long count) {
            super(vec);

            this.count = count;
        }
    }
}
