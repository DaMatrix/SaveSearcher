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
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.minecraft.entity.Entity;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.SearchModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public abstract class AbstractEntityByIdSearchModule<S> extends AbstractSearchModule<S> {
    @NonNull
    private final Set<ResourceLocation> entityIds;

    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        chunk.entities().forEach(entity -> {
            if (this.entityIds.contains(entity.id())) {
                this.processEntity(entity);
            }
        });
    }

    protected abstract void processEntity(@NonNull Entity entity);

    @Override
    public void merge(@NonNull List<SearchModule> in, @NonNull Consumer<SearchModule> addMerged) {
        if (in.stream().filter(AbstractEntityByIdSearchModule.class::isInstance).count() <= 1L) {
            return;
        }

        List<AbstractEntityByIdSearchModule<?>> entitySearchModules = new ArrayList<>();
        for (Iterator<SearchModule> itr = in.iterator(); itr.hasNext(); ) {
            SearchModule module = itr.next();
            if (module instanceof AbstractEntityByIdSearchModule) {
                entitySearchModules.add(uncheckedCast(module));
                itr.remove();
            }
        }

        List<AbstractEntityByIdSearchModule<?>> merged = SearchModule.merge(entitySearchModules, AbstractEntityByIdSearchModule::mergeEntity);
        Map<ResourceLocation, List<AbstractEntityByIdSearchModule<?>>> modulesById = new HashMap<>();
        merged.forEach(module -> module.entityIds.forEach(id -> modulesById.computeIfAbsent(id, _id -> new ArrayList<>()).add(module)));

        addMerged.accept(new AbstractMergedSearchModule(merged) {
            @Override
            public void handle(long current, long estimatedTotal, @NonNull Chunk chunk) {
                chunk.entities().forEach(entity -> {
                    List<AbstractEntityByIdSearchModule<?>> modules = modulesById.get(entity.id());
                    if (modules != null) {
                        modules.forEach(module -> module.processEntity(entity));
                    }
                });
            }
        });
    }

    protected void mergeEntity(@NonNull List<AbstractEntityByIdSearchModule<?>> in, @NonNull Consumer<AbstractEntityByIdSearchModule<?>> addMerged) {
        //no-op
    }
}
