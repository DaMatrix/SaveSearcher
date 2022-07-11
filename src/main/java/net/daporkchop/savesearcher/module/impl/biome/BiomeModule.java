package net.daporkchop.savesearcher.module.impl.biome;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.SearchModule;

public class BiomeModule extends AbstractSearchModule<BiomeModule.BiomeData> {

    public static SearchModule find(@NonNull String[] args) {
        switch (args.length) {
            case 0:
                return new BiomeModule();
            case 1:
                return new BiomeByIdModule(args[0]);
            default:
                throw new IllegalArgumentException("--biome must be called with either no arguments or the biome ID to search for!");
        }
    }


    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        final int x = chunk.getX() << 4;
        final int z = chunk.getZ() << 4;

        for (int xx = 15; xx >= 0; xx--) {
            for (int zz = 15; zz >= 0; zz--) {
                int biomeId = chunk.getBiomeId(x + xx, z + zz);
                handle.accept(new BiomeData(x + xx, z + zz, biomeId));
            }
        }
    }


    @Override
    public String toString() {
        return "Biomes";
    }

    @Override
    public int hashCode() {
        return BiomeModule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BiomeModule;
    }

    @RequiredArgsConstructor
    protected static final class BiomeData {
        public final int x;
        public final int z;
        public final int id;

    }
}
