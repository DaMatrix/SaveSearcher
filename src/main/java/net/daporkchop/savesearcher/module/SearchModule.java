/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2018-2019 DaPorkchop_ and contributors
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it. Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.savesearcher.module;

import lombok.NonNull;
import net.daporkchop.lib.minecraft.region.util.ChunkProcessor;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * The core of all SaveSearcher modules.
 * <p>
 * Implementations of this do the actual searching of the world.
 *
 * @author DaPorkchop_
 */
public interface SearchModule extends ChunkProcessor {
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
     * @return the class of values that will be returned by this module
     */
    Class<?> dataType();
}
