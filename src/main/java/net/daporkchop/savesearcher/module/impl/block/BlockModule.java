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
import net.daporkchop.savesearcher.module.PositionData;
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
                    invert = true;
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
        }

        if (id == null) {
            throw new IllegalArgumentException("No id given!");
        } else if (min > max) {
            throw new IllegalArgumentException(String.format("Min Y must be less than or equal to max Y! (min=%d, max=%d)", min, max));
        } else if (min == 0 && max == 255)  {
            return invert ? new InverseBlockModule(id, meta) : new BlockModule(id, meta);
        } else {
            return invert ? new InverseBlockRangeModule(id, meta, min, max) : new BlockRangeModule(id, meta, min, max);
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

        for (int sectionY = 15; sectionY >= 0; sectionY--) {
            Section section = chunk.section(sectionY);
            if (section == null)    {
                continue;
            }
            for (int x = 15; x >= 0; x--) {
                for (int z = 15; z >= 0; z--) {
                    for (int y = 15; y >= 0; y--) {
                        if (section.getBlockId(x, y, z) == id && (meta == -1 || section.getBlockMeta(x, y, z) == meta)) {
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
