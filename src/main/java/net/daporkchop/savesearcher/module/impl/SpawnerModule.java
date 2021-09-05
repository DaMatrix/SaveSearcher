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
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.savesearcher.module.PositionData;
import net.daporkchop.savesearcher.module.merging.AbstractTileEntityByClassSearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;
import net.daporkchop.savesearcher.tileentity.TileEntitySpawner;

import java.util.Objects;

/**
 * @author DaPorkchop_
 */
public final class SpawnerModule extends AbstractTileEntityByClassSearchModule<SpawnerModule.SpawnerData, TileEntitySpawner> {
    protected final ResourceLocation filterId;

    public SpawnerModule(String[] args) {
        switch (args.length) {
            case 0:
                this.filterId = null;
                break;
            case 1:
                this.filterId = new ResourceLocation(args[0]);
                break;
            default:
                throw new IllegalArgumentException("--spawner must be called with either no arguments or the entity ID of the spawner type to search for!");
        }
    }

    @Override
    protected void handleTileEntity(@NonNull Chunk chunk, @NonNull TileEntitySpawner tileEntity, @NonNull OutputHandle handle) {
        if (this.filterId == null || tileEntity.canSpawn(this.filterId)) {
            handle.accept(new SpawnerData(tileEntity));
        }
    }

    @Override
    public String toString() {
        return this.filterId == null ? "Spawners" : String.format("Spawners (id=%s)", this.filterId);
    }

    @Override
    public int hashCode() {
        return this.filterId == null ? SpawnerModule.class.hashCode() : this.filterId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof SpawnerModule) {
            return Objects.equals(this.filterId, ((SpawnerModule) obj).filterId);
        } else {
            return false;
        }
    }

    protected static final class SpawnerData extends PositionData {
        public final ResourceLocation id;

        public SpawnerData(@NonNull TileEntitySpawner te) {
            super(te);

            if (te.entries().size() != 1) {
                throw new IllegalArgumentException(String.format("Spawner (%d,%d,%d) has %d entries!", te.getX(), te.getY(), te.getZ(), te.entries().size()));
            }

            this.id = te.entries().get(0).id();
        }
    }
}
