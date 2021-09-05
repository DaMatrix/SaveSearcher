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
import net.daporkchop.lib.common.function.io.IOConsumer;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.Section;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public abstract class AbstractChunkSectionSearchModule<S> extends AbstractSearchModule<S> {
    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        for (int sectionY = 0; sectionY < 16; sectionY++) {
            Section section = chunk.section(sectionY);
            if (section == null) {
                continue;
            }
            this.processChunkSection(chunk, section);
        }
    }

    protected abstract void processChunkSection(@NonNull Chunk chunk, @NonNull Section section);

    @Override
    public void merge(@NonNull List<SearchModule> in, @NonNull Consumer<SearchModule> addMerged) {
        if (in.stream().filter(AbstractChunkSectionSearchModule.class::isInstance).count() <= 1L) {
            return;
        }

        List<AbstractChunkSectionSearchModule<?>> modules = new ArrayList<>();
        for (Iterator<SearchModule> itr = in.iterator(); itr.hasNext(); ) {
            SearchModule module = itr.next();
            if (module instanceof AbstractChunkSectionSearchModule) {
                modules.add(uncheckedCast(module));
                itr.remove();
            }
        }

        List<AbstractChunkSectionSearchModule<?>> mergedList = SearchModule.merge(modules, AbstractChunkSectionSearchModule::mergeChunkSection);
        AbstractChunkSectionSearchModule<?>[] mergedArray = uncheckedCast(mergedList.toArray(new AbstractChunkSectionSearchModule[0]));

        addMerged.accept(new AbstractMergedSearchModule(mergedList) {
            @Override
            public void handle(long current, long estimatedTotal, @NonNull Chunk chunk) {
                for (int sectionY = 0; sectionY < 16; sectionY++) {
                    Section section = chunk.section(sectionY);
                    if (section == null) {
                        continue;
                    }

                    for (AbstractChunkSectionSearchModule<?> module : mergedArray) {
                        module.processChunkSection(chunk, section);
                    }
                }
            }
        });
    }

    protected void mergeChunkSection(@NonNull List<AbstractChunkSectionSearchModule<?>> in, @NonNull Consumer<AbstractChunkSectionSearchModule<?>> addMerged) {
        //no-op
    }

    /**
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    protected abstract class Merged extends AbstractChunkSectionSearchModule<Object> {
        @NonNull
        protected final List<? extends SearchModule> allChildren;

        @Override
        public void init(@NonNull World world, @NonNull OutputHandle handle) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<?> dataType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws IOException {
            this.allChildren.forEach((IOConsumer<SearchModule>) SearchModule::close);
        }
    }
}
