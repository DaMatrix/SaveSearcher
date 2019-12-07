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

package net.daporkchop.savesearcher.output.csv;

import lombok.NonNull;
import net.daporkchop.lib.binary.stream.DataOut;
import net.daporkchop.lib.binary.stream.file.BufferingFileOutput;
import net.daporkchop.lib.common.function.PFunctions;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.lib.reflection.PField;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author DaPorkchop_
 */
public final class CSVOutputHandle implements OutputHandle {
    protected final File parent;

    protected Class<?> clazz;
    protected PField[] fields;
    protected DataOut out;

    public CSVOutputHandle(@NonNull File parent)    {
        this.parent = PFiles.ensureDirectoryExists(parent);
    }

    @Override
    public void init(@NonNull SearchModule module) {
        this.fields = Arrays.stream((this.clazz = module.dataType()).getFields())
                .map(PField::of)
                .filter(PFunctions.not(PField::isTransient))
                .toArray(PField[]::new);
        try {
            this.out = new BufferingFileOutput(new File(this.parent, module + ".csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.out.close();
        } finally {
            this.out = null;
        }
    }

    @Override
    public void accept(@NonNull Object data) {
        if (data.getClass() != this.clazz)  {
            throw new IllegalArgumentException(String.format("Expected %s but got %s!", this.clazz, data.getClass()));
        }
    }
}
