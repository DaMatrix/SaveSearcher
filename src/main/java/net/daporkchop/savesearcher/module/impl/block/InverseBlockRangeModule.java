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

import lombok.NonNull;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.PositionData;
import net.daporkchop.savesearcher.module.PositionDataXZ;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
public final class InverseBlockRangeModule extends AbstractSearchModule<PositionDataXZ> {
    protected ResourceLocation searchName;
    protected int meta = -1;
    protected int id;
    protected int minY = 0;
    protected int maxY = 255;

    public InverseBlockRangeModule(String[] args) {
        for (String s : args) {
            if (s.isEmpty()) {
                continue;
            }
            String[] split = s.split("=");
            if (split.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
            switch (split[0]) {
                case "id": {
                    this.searchName = new ResourceLocation(split[1]);
                }
                break;
                case "meta": {
                    this.meta = Integer.parseInt(split[1]);
                    if (this.meta > 15 || this.meta < 0) {
                        throw new IllegalArgumentException(String.format("Invalid meta: %d (must be in range 0-15)", this.meta));
                    }
                }
                break;
                case "min":
                case "minY": {
                    this.minY = Integer.parseInt(split[1]);
                }
                break;
                case "max":
                case "maxY": {
                    this.maxY = Integer.parseInt(split[1]);
                }
                break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
        }
        if (this.searchName == null) {
            throw new IllegalArgumentException("No id given!");
        } else if (this.minY > this.maxY) {
            throw new IllegalArgumentException(String.format("Min Y must be less than or equal to max Y! (min=%d, max=%d)", this.minY, this.maxY));
        }
    }

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
        final int maxY = this.maxY;
        final int minY = this.minY;

        for (int x = 15; x >= 0; x--) {
            for (int z = 15; z >= 0; z--) {
                for (int y = maxY; y >= minY; y--) {
                    if (chunk.getBlockId(x, y, z) == id && (meta == -1 || chunk.getBlockMeta(x, y, z) == meta))  {
                        return;
                    }
                }
            }
        }

        handle.accept(new PositionDataXZ(chunk.pos()));
    }

    @Override
    public String toString() {
        return String.format("Block - Inverted,Ranged (id=%s, meta=%d, min=%d, max=%d)", this.searchName.toString(), this.meta, this.minY, this.maxY);
    }

    @Override
    public int hashCode() {
        return ((this.searchName.hashCode() * 31 + this.meta) * 31 + this.maxY) * 31 + this.minY;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj.getClass() == InverseBlockRangeModule.class) {
            InverseBlockRangeModule other = (InverseBlockRangeModule) obj;
            return this.searchName.equals(other.searchName) && this.meta == other.meta && this.maxY == other.maxY && this.minY == other.minY;
        } else {
            return false;
        }
    }
}
