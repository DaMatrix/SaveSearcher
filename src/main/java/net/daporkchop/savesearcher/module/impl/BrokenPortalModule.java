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
import net.daporkchop.lib.math.vector.i.Vec3i;
import net.daporkchop.lib.minecraft.region.util.NeighboringChunkProcessor;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.util.BlockAccess;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

public final class BrokenPortalModule extends AbstractSearchModule<Vec3i> implements NeighboringChunkProcessor {

    protected int portal_id;
    protected int obsidian_id;

    public BrokenPortalModule(String[] args) {
    }

    @Override
    public void init(@NonNull World world, @NonNull OutputHandle handle) {
        super.init(world, handle);

        this.portal_id = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(new ResourceLocation("minecraft:portal"));
        this.obsidian_id = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(new ResourceLocation("minecraft:obsidian"));
    }

    @Override
    public void handle(long l, long l1, Chunk chunk) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handle(long current, long estimatedTotal, Chunk chunk, BlockAccess access) {
        final int portal = this.portal_id;
        final int obsidian = this.obsidian_id;

        final int x = chunk.getX() << 4;
        final int z = chunk.getZ() << 4;

        for (int xx = 15; xx >= 0; xx--) {
            for (int zz = 15; zz >= 0; zz--) {
                for (int y = 255; y >= 0; y--) {
                    if (access.getBlockId(x + xx, y, z + zz) == portal) {
                        int meta = access.getBlockMeta(x + xx, y, z + zz); // x = 1, z = 2, 0 = unknown?

                        switch (meta) {
                            case 1:
                                int east_id = access.getBlockId(x + xx + 1, y, z + zz);
                                int west_id = access.getBlockId(x + xx - 1, y, z + zz);

                                if ((east_id != portal
                                        && east_id != obsidian)
                                        || (west_id != portal
                                        && west_id != obsidian)) {
                                    handle.accept(new Vec3i(x + xx, y, z + zz));
                                }
                                break;
                            case 2:
                                int south_id = access.getBlockId(x + xx, y, z + zz + 1);
                                int north_id = access.getBlockId(x + xx, y, z + zz - 1);

                                if ((south_id != portal
                                        && south_id != obsidian)
                                        || (north_id != portal
                                        && north_id != obsidian)) {
                                    handle.accept(new Vec3i(x + xx, y, z + zz));
                                }
                                break;

                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Broken Portals";
    }

    @Override
    public int hashCode() {
        return BrokenPortalModule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BrokenPortalModule;
    }
}
