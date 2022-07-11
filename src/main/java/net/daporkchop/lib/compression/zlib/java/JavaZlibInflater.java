/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2018-2022 DaPorkchop_
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

package net.daporkchop.lib.compression.zlib.java;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.daporkchop.lib.compression.PInflater;
import net.daporkchop.lib.compression.util.exception.ContextFinishedException;
import net.daporkchop.lib.compression.util.exception.ContextFinishingException;
import net.daporkchop.lib.compression.zlib.ZlibInflater;
import net.daporkchop.lib.compression.zlib.java.JavaZlib;
import net.daporkchop.lib.natives.util.exception.InvalidBufferTypeException;
import net.daporkchop.lib.unsafe.util.AbstractReleasable;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author DaPorkchop_
 */
@Accessors(fluent = true)
public class JavaZlibInflater extends AbstractReleasable implements ZlibInflater {
    @Getter
    private final JavaZlib provider;

    private final Inflater inflater;

    public JavaZlibInflater(@NonNull JavaZlib provider, boolean raw) {
        this.provider = provider;
        this.inflater = new Inflater(raw);
    }

    @Override
    public PInflater dict(@NonNull ByteBuf dict) throws InvalidBufferTypeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean fullInflate(@NonNull ByteBuf src, @NonNull ByteBuf dst) throws InvalidBufferTypeException {
        throw new UnsupportedOperationException();
    }

    @Override
    @SneakyThrows(IOException.class)
    public void fullInflateGrowing(@NonNull ByteBuf src, @NonNull ByteBuf dst) throws InvalidBufferTypeException {
        try (InputStream in = new InflaterInputStream(new ByteBufInputStream(src), this.inflater)) {
            byte[] buf = new byte[4096];
            for (int i; (i = in.read(buf)) >= 0; ) {
                dst.writeBytes(buf, 0, i);
            }
        }
    }

    @Override
    public PInflater update(boolean flush) throws ContextFinishedException, ContextFinishingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean finish() throws ContextFinishedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PInflater src(@NonNull ByteBuf src) throws InvalidBufferTypeException, ContextFinishingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PInflater dst(@NonNull ByteBuf dst) throws InvalidBufferTypeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PInflater reset() {
        this.inflater.reset();
        return this;
    }

    @Override
    public boolean directAccepted() {
        return true;
    }

    @Override
    protected void doRelease() {
        this.inflater.end();
    }
}
