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

package net.daporkchop.savesearcher.module.merging;

import lombok.NonNull;
import net.daporkchop.lib.common.util.GenericMatcher;
import net.daporkchop.lib.minecraft.tileentity.TileEntity;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
public abstract class AbstractTileEntityByClassSearchModule<S, T extends TileEntity> extends AbstractSearchModule<S> {
    protected final Class<T> tileEntityClass = GenericMatcher.uncheckedFind(this.getClass(), AbstractTileEntityByClassSearchModule.class, "T");

    @Override
    protected void processChunk(@NonNull Chunk chunk, @NonNull OutputHandle handle) {
        chunk.tileEntities().forEach(tileEntity -> {
            if (this.tileEntityClass == tileEntity.getClass()) {
                this.handleTileEntity(chunk, uncheckedCast(tileEntity), handle);
            }
        });
    }

    protected abstract void handleTileEntity(@NonNull Chunk chunk, @NonNull T tileEntity, @NonNull OutputHandle handle);

    @Override
    public void merge(@NonNull List<SearchModule> in, @NonNull Consumer<SearchModule> addMerged) {
        if (in.stream().filter(AbstractTileEntityByClassSearchModule.class::isInstance).count() <= 1L) {
            return;
        }

        List<AbstractTileEntityByClassSearchModule<?, ?>> tileEntitySearchModules = new ArrayList<>();
        for (Iterator<SearchModule> itr = in.iterator(); itr.hasNext(); ) {
            SearchModule module = itr.next();
            if (module instanceof AbstractTileEntityByClassSearchModule) {
                tileEntitySearchModules.add(uncheckedCast(module));
                itr.remove();
            }
        }

        List<AbstractTileEntityByClassSearchModule<?, ?>> merged = SearchModule.merge(tileEntitySearchModules, AbstractTileEntityByClassSearchModule::mergeTileEntity);
        Map<Class<? extends TileEntity>, List<AbstractTileEntityByClassSearchModule<?, ?>>> modulesByClass = new IdentityHashMap<>();
        merged.forEach(module -> modulesByClass.computeIfAbsent(module.tileEntityClass, clazz -> new ArrayList<>()).add(module));

        addMerged.accept(new AbstractMergedSearchModule(merged) {
            @Override
            public void handle(long current, long estimatedTotal, @NonNull Chunk chunk) {
                chunk.tileEntities().forEach(tileEntity -> {
                    List<AbstractTileEntityByClassSearchModule<?, ?>> modules = modulesByClass.get(tileEntity.getClass());
                    if (modules != null) {
                        modules.forEach(module -> module.handleTileEntity(chunk, uncheckedCast(tileEntity), module.handle()));
                    }
                });
            }
        });
    }

    protected void mergeTileEntity(@NonNull List<AbstractTileEntityByClassSearchModule<?, ?>> in, @NonNull Consumer<AbstractTileEntityByClassSearchModule<?, ?>> addMerged) {
        //no-op
    }
}
