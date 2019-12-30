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
import net.daporkchop.lib.binary.oio.writer.UTF8FileWriter;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.lib.common.pool.handle.DefaultThreadHandledPool;
import net.daporkchop.lib.common.pool.handle.Handle;
import net.daporkchop.lib.common.pool.handle.HandledPool;
import net.daporkchop.lib.common.system.OperatingSystem;
import net.daporkchop.lib.common.system.PlatformInfo;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.reflection.PField;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author DaPorkchop_
 */
public class CSVOutputHandle implements OutputHandle {
    protected static final HandledPool<StringBuilder> BUILDER_CACHE = new DefaultThreadHandledPool<>(StringBuilder::new, 2);

    protected final File parent;

    protected Class<?>                       clazz;
    protected List<PField<?>>                fields;
    protected List<Function<Object, String>> mappers;
    protected Writer                         writer;

    public CSVOutputHandle(@NonNull File parent) {
        this.parent = PFiles.ensureDirectoryExists(parent);
    }

    @Override
    public void init(@NonNull SearchModule module) {
        this.fields = new ArrayList<>();

        Class<?> clazz = this.clazz = module.dataType();
        while (clazz != Object.class) {
            int i = 0;
            for (PField field : Arrays.stream(clazz.getDeclaredFields())
                    .map(PField::of)
                    .filter(field -> !field.isTransient() && !field.isSynthetic()).toArray(PField[]::new)) {
                this.fields.add(i++, field);
            }
            clazz = clazz.getSuperclass();
        }

        this.mappers = this.fields.stream()
                .map((Function<PField, Function<Object, String>>) field -> {
                    switch (field.getType()) {
                        case BOOLEAN:
                            return o -> Boolean.toString(field.getBoolean(o));
                        case SHORT:
                            return o -> Short.toString(field.getShort(o));
                        case CHAR:
                            return o -> Character.toString(field.getChar(o));
                        case INT:
                            return o -> Integer.toString(field.getInt(o));
                        case LONG:
                            return o -> Long.toString(field.getLong(o));
                        case FLOAT:
                            return o -> Float.toString(field.getFloat(o));
                        case DOUBLE:
                            return o -> Double.toString(field.getDouble(o));
                        case OBJECT:
                            return o -> Objects.toString(field.get(o));
                        default:
                            throw new IllegalArgumentException(field.getType().name());
                    }
                })
                .collect(Collectors.toList());

        try {
            this.writer = this.createWriter(module);

            this.writer.append(this.fields.stream().map(PField::getName).collect(() -> new StringJoiner(","), StringJoiner::add, StringJoiner::merge).toString());
            this.writer.append(PlatformInfo.OPERATING_SYSTEM.lineEnding());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Writer createWriter(@NonNull SearchModule module) throws IOException {
        String name = module + ".csv";
        if (PlatformInfo.OPERATING_SYSTEM == OperatingSystem.Windows)   {
            name = name.replace(':', '_');
        }
        return new UTF8FileWriter(PFiles.ensureFileExists(new File(this.parent, name)));
    }

    @Override
    public void close() throws IOException {
        try {
            this.writer.close();
        } finally {
            this.writer = null;
        }
    }

    @Override
    public void accept(@NonNull Object data) {
        if (data.getClass() != this.clazz) {
            throw new IllegalArgumentException(String.format("Expected %s but got %s!", this.clazz, data.getClass()));
        }

        try (Handle<StringBuilder> handle = BUILDER_CACHE.get()) {
            StringBuilder builder = handle.value();
            builder.setLength(0);
            for (Function<Object, String> mapper : this.mappers) {
                builder.append(CSVUtil.escape(mapper.apply(data))).append(',');
            }
            builder.setLength(builder.length() - 1);

            synchronized (this) {
                this.writer.append(builder);
                this.writer.append(PlatformInfo.OPERATING_SYSTEM.lineEnding());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
