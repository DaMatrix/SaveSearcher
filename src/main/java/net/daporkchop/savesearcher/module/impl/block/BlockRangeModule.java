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

import com.google.gson.JsonObject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.math.vector.i.Vec3i;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.PositionData;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
final class BlockRangeModule extends AbstractSearchModule<Vec3i> {
    protected final ResourceLocation searchName;
    protected final int meta;
    protected final int minY;
    protected final int maxY;
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
        final int maxY = this.maxY;
        final int minY = this.minY;

        for (int x = 15; x >= 0; x--) {
            for (int z = 15; z >= 0; z--) {
                for (int y = maxY; y >= minY; y--) {
                    if (chunk.getBlockId(x, y, z) == id && (meta == -1 || chunk.getBlockMeta(x, y, z) == meta))  {
                        handle.accept(new Vec3i(chunk.minX() + x, y, chunk.minZ() + z));
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        if (this.meta == -1)    {
            return String.format("Block - Ranged (id=%s, min=%d, max=%d)", this.searchName, this.minY, this.maxY);
        } else {
            return String.format("Block - Ranged (id=%s, meta=%d, min=%d, max=%d)", this.searchName, this.meta, this.minY, this.maxY);
        }
    }

    @Override
    public int hashCode() {
        return ((this.searchName.hashCode() * 31 + this.meta) * 31 + this.maxY) * 31 + this.minY;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj.getClass() == BlockRangeModule.class) {
            BlockRangeModule other = (BlockRangeModule) obj;
            return this.searchName.equals(other.searchName) && this.meta == other.meta && this.maxY == other.maxY && this.minY == other.minY;
        } else {
            return false;
        }
    }
}
