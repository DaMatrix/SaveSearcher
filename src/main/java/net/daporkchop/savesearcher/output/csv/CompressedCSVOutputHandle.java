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

package net.daporkchop.savesearcher.output.csv;

import lombok.NonNull;
import net.daporkchop.lib.binary.oio.appendable.PAppendable;
import net.daporkchop.lib.binary.oio.writer.UTF8FileWriter;
import net.daporkchop.lib.common.system.PlatformInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * @author DaPorkchop_
 */
public final class CompressedCSVOutputHandle<R> extends CSVOutputHandle<R> {
    public CompressedCSVOutputHandle(@NonNull File parent) {
        super(parent);
    }

    @Override
    protected PAppendable createWriter(@NonNull File file) throws IOException {
        return new UTF8FileWriter(new GZIPOutputStream(new FileOutputStream(file)), PlatformInfo.OPERATING_SYSTEM.lineEnding(), false);
    }

    @Override
    protected String getFileSuffix() {
        return ".csv.gz";
    }
}
