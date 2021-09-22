package net.daporkchop.savesearcher.module.merging;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.tileentity.TileEntity;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.SearchModule;

import java.util.*;
import java.util.function.Consumer;

import static net.daporkchop.lib.common.util.PorkUtil.uncheckedCast;

@RequiredArgsConstructor
public abstract class AbstractTileEntityByIdSearchModule<S> extends AbstractSearchModule<S> {
    @NonNull
    private final Set<ResourceLocation> tileEntityIds;

    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        chunk.tileEntities().forEach(te -> {
            if (this.tileEntityIds.contains(te.id())) {
                this.processTileEntity(te);
            }
        });
    }

    protected abstract void processTileEntity(@NonNull TileEntity entity);

    @Override
    public void merge(@NonNull List<SearchModule> in, @NonNull Consumer<SearchModule> addMerged) {
        if (in.stream().filter(AbstractTileEntityByIdSearchModule.class::isInstance).count() <= 1L) {
            return;
        }

        List<AbstractTileEntityByIdSearchModule<?>> tileEntitySearchModules = new ArrayList<>();
        for (Iterator<SearchModule> itr = in.iterator(); itr.hasNext(); ) {
            SearchModule module = itr.next();
            if (module instanceof AbstractTileEntityByIdSearchModule) {
                tileEntitySearchModules.add(uncheckedCast(module));
                itr.remove();
            }
        }

        List<AbstractTileEntityByIdSearchModule<?>> merged = SearchModule.merge(tileEntitySearchModules, AbstractTileEntityByIdSearchModule::mergeEntity);
        Map<ResourceLocation, List<AbstractTileEntityByIdSearchModule<?>>> modulesById = new HashMap<>();
        merged.forEach(module -> module.tileEntityIds.forEach(id -> modulesById.computeIfAbsent(id, _id -> new ArrayList<>()).add(module)));

        addMerged.accept(new AbstractMergedSearchModule(merged) {
            @Override
            public void handle(long current, long estimatedTotal, @NonNull Chunk chunk) {
                chunk.tileEntities().forEach(te -> {
                    List<AbstractTileEntityByIdSearchModule<?>> modules = modulesById.get(te.id());
                    if (modules != null) {
                        modules.forEach(module -> module.processTileEntity(te));
                    }
                });
            }
        });
    }

    protected void mergeEntity(@NonNull List<AbstractTileEntityByIdSearchModule<?>> in, @NonNull Consumer<AbstractTileEntityByIdSearchModule<?>> addMerged) {
        //no-op
    }
}
