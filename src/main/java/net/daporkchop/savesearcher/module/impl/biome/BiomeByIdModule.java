package net.daporkchop.savesearcher.module.impl.biome;

import lombok.NonNull;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.savesearcher.module.AbstractSearchModule;

public class BiomeByIdModule extends AbstractSearchModule<BiomeModule.BiomeData> {
    protected final int biomeId;

    public BiomeByIdModule(String id) {
        biomeId = Integer.parseInt(id);
    }


    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        final int x = chunk.getX() << 4;
        final int z = chunk.getZ() << 4;

        for (int xx = 15; xx >= 0; xx--) {
            for (int zz = 15; zz >= 0; zz--) {
                int id = chunk.getBiomeId(x + xx, z + zz);
                if (id == biomeId) {
                    handle.accept(new BiomeModule.BiomeData(x + xx, z + zz, id));
                }
            }
        }
    }


    @Override
    public String toString() {
        return String.format("Biomes (id=%s)", biomeId);
    }
}
