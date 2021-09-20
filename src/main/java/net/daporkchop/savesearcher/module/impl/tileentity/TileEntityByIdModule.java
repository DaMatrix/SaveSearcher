package net.daporkchop.savesearcher.module.impl.tileentity;

import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.tileentity.TileEntity;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.savesearcher.module.merging.AbstractTileEntityByIdSearchModule;

@EqualsAndHashCode(callSuper = false)
final class TileEntityByIdModule extends AbstractTileEntityByIdSearchModule<TileEntityModule.TileEntityData> {
    protected final ResourceLocation filterId;

    public TileEntityByIdModule(@NonNull ResourceLocation filterId) {
        super(ImmutableSet.of(filterId));

        this.filterId = filterId;
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        chunk.tileEntities().forEach(te -> {
            if (this.filterId.equals(te.id())) {
                this.handle.accept(new TileEntityModule.TileEntityData(te));
            }
        });
    }

    @Override
    protected void processTileEntity(@NonNull TileEntity te) {
        this.handle.accept(new TileEntityModule.TileEntityData(te));
    }

    @Override
    public String toString() {
        return String.format("Tile Entities (id=%s)", this.filterId);
    }
}
