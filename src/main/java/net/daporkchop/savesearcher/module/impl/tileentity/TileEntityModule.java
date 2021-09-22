package net.daporkchop.savesearcher.module.impl.tileentity;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.tileentity.TileEntity;
import net.daporkchop.lib.minecraft.tileentity.impl.UnknownTileEntity;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.util.NBTHelper;

public final class TileEntityModule extends AbstractSearchModule<TileEntityModule.TileEntityData> {
    public static SearchModule find(@NonNull String[] args) {
        switch (args.length) {
            case 0:
                return new TileEntityModule();
            case 1:
                return new TileEntityByIdModule(new ResourceLocation(args[0]));
            default:
                throw new IllegalArgumentException("--tileentity must be called with either no arguments or the entity ID to search for!");
        }
    }


    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        chunk.tileEntities()
                .forEach(te -> this.handle.accept(new TileEntityData(te)));
    }



    @Override
    public String toString() {
        return "Tile Entities";
    }

    @Override
    public int hashCode() {
        return TileEntityModule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TileEntityModule;
    }


    @RequiredArgsConstructor
    protected static final class TileEntityData {
        @NonNull
        public final ResourceLocation id;
        public final double x;
        public final double y;
        public final double z;
        @NonNull
        public final String nbt;

        public TileEntityData(@NonNull TileEntity te) {
            this(te.id(), te.getX(), te.getY(), te.getZ(),
                    te instanceof UnknownTileEntity ? NBTHelper.toJson(((UnknownTileEntity) te).data()) : "{}");
        }
    }
}
