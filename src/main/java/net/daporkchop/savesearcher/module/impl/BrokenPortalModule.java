package net.daporkchop.savesearcher.module.impl;

import lombok.NonNull;
import net.daporkchop.lib.math.vector.i.Vec3i;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.Section;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

public class BrokenPortalModule extends AbstractSearchModule<Vec3i> {

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
    protected void processChunk(Chunk chunk, OutputHandle handle) {
        final int portal = this.portal_id;
        final int obsidian = this.obsidian_id;


        for (int sectionY = 15; sectionY >= 0; sectionY--) {
            Section section = chunk.section(sectionY);
            if (section == null) {
                continue;
            }
            for (int x = 15; x >= 0; x--) {
                for (int y = 15; y >= 0; y--) {
                    for (int z = 15; z >= 0; z--) {
                        if (section.getBlockId(x, y, z) == portal) {
                            int meta = section.getBlockMeta(x, y, z); // x = 1, z = 2, 0 = unknown?

                            switch (meta) {
                                case 1:
                                    int east_id = section.getBlockId(x + 1, y, z);
                                    int west_id = section.getBlockId(x - 1, y, z);

                                    if (!(east_id == portal
                                            || east_id == obsidian
                                            || west_id == portal
                                            || west_id == obsidian)) {
                                        handle.accept(new Vec3i(chunk.minX() + x, (section.getY() << 4) + y, chunk.minZ() + z));
                                    }
                                    break;
                                case 2:
                                    int south_id = section.getBlockId(x, y, z + 1);
                                    int north_id = section.getBlockId(x, y, z - 1);

                                    if (!(south_id == portal
                                            || south_id == obsidian
                                            || north_id == portal
                                            || north_id == obsidian)) {
                                        handle.accept(new Vec3i(chunk.minX() + x, (section.getY() << 4) + y, chunk.minZ() + z));
                                    }
                                    break;
                            }


                            return;
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
