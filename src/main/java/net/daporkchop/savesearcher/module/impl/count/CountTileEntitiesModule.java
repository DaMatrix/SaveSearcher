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
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class CountTileEntitiesModule extends AbstractSearchModule<CountBlocksModule.CountData> {
    protected final ResourceLocation filterId;

    @Override
    protected void processChunk(@NonNull Chunk chunk, @NonNull OutputHandle handle) {
        long count;
        if (this.filterId == null)  {
            count = chunk.tileEntities().size();
        } else {
            count = chunk.tileEntities().stream().filter(tileEntity -> this.filterId.equals(tileEntity.id())).count();
        }
        handle.accept(new CountBlocksModule.CountData(chunk.pos(), count));
    }

    @Override
    public String toString() {
        if (this.filterId == null) {
            return "Count - Tile Entities";
        } else {
            return String.format("Count - Tile Entities (id=%s)", this.filterId);
        }
    }

    @Override
    public int hashCode() {
        return this.filterId == null ? CountTileEntitiesModule.class.hashCode() : this.filterId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)    {
            return true;
        } else if (obj instanceof CountTileEntitiesModule)    {
            CountTileEntitiesModule other = (CountTileEntitiesModule) obj;
            if (this.filterId == null)  {
                return other.filterId == null;
            } else {
                return this.filterId.equals(other.filterId);
            }
        } else {
            return false;
        }
    }
}
