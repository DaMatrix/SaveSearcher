/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2018-2020 DaPorkchop_
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
import net.daporkchop.mcworldlib.save.Save;
import net.daporkchop.mcworldlib.world.Chunk;
import net.daporkchop.mcworldlib.world.World;
import net.daporkchop.mcworldlib.world.section.Section;
import net.daporkchop.savesearcher.output.OutputHandle;

import java.io.IOException;

/**
 * The core of all SaveSearcher modules.
 * <p>
 * Modules will only be initialized once, and will be closed after all search operations are completed. Once initialized, implementations
 * must be safely usable from multiple threads.
 *
 * @param <R> the result data type
 * @author DaPorkchop_
 */
public interface SearchModule<R> extends AutoCloseable {
    /**
     * Initializes this module for searching the given save.
     * <p>
     * This may be used to e.g. prefetch numeric block IDs from the registry.
     *
     * @param save   the save that is going to be searched
     * @param handle the {@link OutputHandle} that any output data should be given to
     */
    void init(@NonNull Save save, @NonNull OutputHandle<R> handle);

    /**
     * Closes this module.
     * <p>
     * This will cause any unwritten data to be flushed to disk, and the module will no longer be usable.
     *
     * @throws IOException if an IO exception occurs while closing the module
     */
    @Override
    void close() throws IOException;

    interface ForChunk {
        void acceptChunk(@NonNull World world, @NonNull Chunk chunk);
    }

    interface ForSection {
        void acceptChunk(@NonNull World world, @NonNull Section section);
    }
}
