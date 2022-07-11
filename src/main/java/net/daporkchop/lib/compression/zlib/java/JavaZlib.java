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

import net.daporkchop.lib.common.util.PValidation;
import net.daporkchop.lib.compression.zlib.Zlib;
import net.daporkchop.lib.compression.zlib.ZlibCCtx;
import net.daporkchop.lib.compression.zlib.ZlibDCtx;
import net.daporkchop.lib.compression.zlib.ZlibDeflater;
import net.daporkchop.lib.compression.zlib.ZlibInflater;
import net.daporkchop.lib.compression.zlib.ZlibProvider;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public final class JavaZlib implements ZlibProvider {
    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public boolean directAccepted() {
        return true;
    }

    @Override
    public long compressBoundLong(long srcSize, int mode) {
        //extracted from deflate.c, i'm assuming that the java implementation has the same limits
        PValidation.notNegative(srcSize);
        long conservativeUpperBound = srcSize + ((srcSize + 7L) >> 3L) + ((srcSize + 63L) >> 6L) + 5L;
        switch (mode) {
            case Zlib.MODE_ZLIB:
                return conservativeUpperBound + 6L + 4L; //additional +4 in case `strstart`? whatever that means
            case Zlib.MODE_GZIP:
                return conservativeUpperBound + 18L; //assume there is no gzip message
            case Zlib.MODE_RAW:
                return conservativeUpperBound;
            default:
                throw new IllegalArgumentException("Invalid Zlib compression mode: " + mode);
        }
    }

    @Override
    public ZlibDeflater deflater(int level, int strategy, int mode) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public ZlibInflater inflater(int mode) {
        checkArg(mode != Zlib.MODE_GZIP, "gzip mode not supported!");
        return new JavaZlibInflater(this, mode == Zlib.MODE_RAW);
    }

    @Override
    public ZlibCCtx compressionContext(int level, int strategy, int mode) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public ZlibDCtx decompressionContext(int mode) {
        throw new UnsupportedOperationException(); //TODO
    }
}
