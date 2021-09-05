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

package net.daporkchop.savesearcher.module;

import lombok.NonNull;
import net.daporkchop.lib.common.function.plain.TriConsumer;
import net.daporkchop.lib.minecraft.region.util.ChunkProcessor;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.output.OutputHandle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The core of all SaveSearcher modules.
 * <p>
 * Implementations of this do the actual searching of the world.
 *
 * @author DaPorkchop_
 */
public interface SearchModule extends ChunkProcessor, AutoCloseable {
    static <M> List<M> merge(@NonNull List<M> modules, @NonNull TriConsumer<M, List<M>, Consumer<M>> mergeFunction) {
        modules = new LinkedList<>(modules);
        List<M> newModules = new LinkedList<>();

        //try to merge all modules with each other until nothing happens any more
        OUTER_LOOP:
        while (true) {
            for (M module : modules) {
                mergeFunction.accept(module, modules, newModules::add);

                if (!newModules.isEmpty()) {
                    modules.addAll(newModules);
                    newModules.clear();
                    continue OUTER_LOOP;
                }
            }

            return new ArrayList<>(modules);
        }
    }

    /**
     * Initializes this module for searching the given world.
     * <p>
     * This may be used to e.g. obtain any numeric block IDs needed.
     *
     * @param world  the world to search in
     * @param handle the {@link OutputHandle} that any output data should be given to
     */
    void init(@NonNull World world, @NonNull OutputHandle handle);

    /**
     * Closes this module.
     * <p>
     * This will cause any unwritten data to be flushed to disk, and the module will no longer be usable.
     *
     * @throws IOException if an IO exception occurs while closing the module
     */
    @Override
    void close() throws IOException;

    /**
     * @return the class of values that will be returned by this module
     */
    Class<?> dataType();

    /**
     * Attempts to merge some of the given search modules.
     *
     * @param in the list of input modules
     * @param addMerged a callback function to pass newly merged modules to
     */
    default void merge(@NonNull List<SearchModule> in, @NonNull Consumer<SearchModule> addMerged) {
        //no-op
    }
}
