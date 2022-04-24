package net.daporkchop.savesearcher.module.impl;

import lombok.NonNull;
import net.daporkchop.lib.math.vector.i.Vec2i;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.SearchModule;

public class BiomeModule extends AbstractSearchModule<Vec2i> {
    protected final int biomeId;

    public BiomeModule(int id) {
        biomeId = id;
    }

    public static SearchModule find(@NonNull String[] args) {
        if (args.length > 0) {
            return new BiomeModule(Integer.parseInt(args[0]));
        } else {
            throw new RuntimeException("Biome id");
        }
    }


    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        final int x = chunk.getX() << 4;
        final int z = chunk.getZ() << 4;

        for (int xx = 15; xx >= 0; xx--) {
            for (int zz = 15; zz >= 0; zz--) {
                if (chunk.getBiomeId(x + xx, z + zz) == biomeId) {
                    handle.accept(new Vec2i(x + xx, z + zz));
                }
            }
        }
    }



    @Override
    public String toString() {
        return String.format("Biomes (id=%s)", biomeId);
    }

    @Override
    public int hashCode() {
        return BiomeModule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BiomeModule;
    }
}
