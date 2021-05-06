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

package net.daporkchop.savesearcher.module.impl;

import lombok.NonNull;
import net.daporkchop.lib.common.ref.Ref;
import net.daporkchop.lib.common.ref.ThreadRef;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.PositionData;
import net.daporkchop.savesearcher.output.OutputHandle;
import net.daporkchop.savesearcher.tileentity.TileEntityCommandBlock;

import java.util.regex.Matcher;

/**
 * @author DaPorkchop_
 */
public final class CommandBlockModule extends AbstractSearchModule<CommandBlockModule.CommandBlockData> {
    protected final Ref<Matcher> matcherCache;

    public CommandBlockModule(String[] args) {
        switch (args.length) {
            case 1:
                this.matcherCache = null;
                break;
            case 2:
                this.matcherCache = ThreadRef.regex(args[1]);
                break;
            default:
                throw new IllegalArgumentException("--command_block must be called with either no arguments or a regex matching the command to search for!");
        }
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk, @NonNull OutputHandle handle) {
        if (this.matcherCache == null) {
            chunk.tileEntities().stream()
                    .filter(TileEntityCommandBlock.class::isInstance)
                    .map(te -> new CommandBlockData((TileEntityCommandBlock) te))
                    .forEach(handle::accept);
        } else {
            Matcher matcher = this.matcherCache.get();
            chunk.tileEntities().stream()
                    .filter(TileEntityCommandBlock.class::isInstance)
                    .map(TileEntityCommandBlock.class::cast)
                    .filter(te -> matcher.reset(te.command()).matches())
                    .map(CommandBlockData::new)
                    .forEach(handle::accept);
        }
    }

    @Override
    public String toString() {
        return this.matcherCache == null ? "Command Blocks" : String.format("Command Blocks (regex=%s)", this.matcherCache.get().pattern().pattern());
    }

    @Override
    public int hashCode() {
        return this.matcherCache == null ? CommandBlockModule.class.hashCode() : this.matcherCache.get().pattern().pattern().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof CommandBlockModule) {
            if (this.matcherCache == null) {
                return ((CommandBlockModule) obj).matcherCache == null;
            } else {
                return ((CommandBlockModule) obj).matcherCache != null
                       && this.matcherCache.get().pattern().pattern().equals(((CommandBlockModule) obj).matcherCache.get().pattern().pattern());
            }
        } else {
            return false;
        }
    }

    protected static final class CommandBlockData extends PositionData {
        public final String command;
        public final String lastOutput;

        public CommandBlockData(@NonNull TileEntityCommandBlock te) {
            super(te);

            this.command = te.command();
            this.lastOutput = te.lastOutput();
        }
    }
}
