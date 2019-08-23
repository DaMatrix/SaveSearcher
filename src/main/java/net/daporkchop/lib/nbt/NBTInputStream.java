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

package net.daporkchop.lib.nbt;

import lombok.NonNull;
import net.daporkchop.lib.binary.UTF8;
import net.daporkchop.lib.binary.stream.DataIn;
import net.daporkchop.lib.encoding.compression.Compression;
import net.daporkchop.lib.encoding.compression.CompressionHelper;
import net.daporkchop.lib.nbt.tag.TagRegistry;
import net.daporkchop.lib.nbt.tag.notch.CompoundTag;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author DaPorkchop_
 */
public class NBTInputStream extends DataIn {
    private final InputStream in;
    private final TagRegistry defaultRegistry;

    public NBTInputStream(@NonNull InputStream in) throws IOException {
        this(in, Compression.NONE, TagRegistry.NOTCHIAN);
    }

    public NBTInputStream(@NonNull InputStream in, @NonNull CompressionHelper compression) throws IOException {
        this(in, compression, TagRegistry.NOTCHIAN);
    }

    public NBTInputStream(@NonNull InputStream in, @NonNull TagRegistry registry) throws IOException {
        this(in, Compression.NONE, registry);
    }

    public NBTInputStream(@NonNull InputStream in, @NonNull CompressionHelper compression, @NonNull TagRegistry registry) throws IOException {
        this.in = new BufferedInputStream(compression.inflate(in), 32768);
        this.defaultRegistry = registry;
    }

    public CompoundTag readTag() throws IOException {
        return this.readTag(this.defaultRegistry);
    }

    public CompoundTag readTag(@NonNull TagRegistry registry) throws IOException {
        byte id = this.readByte();
        if (registry.getId(CompoundTag.class) != id) {
            throw new IllegalStateException("Invalid id for compound tag!");
        }
        byte[] b = new byte[this.readShort() & 0xFFFF];
        this.readFully(b, 0, b.length);
        CompoundTag tag = new CompoundTag(new String(b, UTF8.utf8));
        tag.read(this, registry);
        return tag;
    }

    //inpustream implementations

    @Override
    public void close() throws IOException {
        this.in.close();
    }

    @Override
    public int read() throws IOException {
        return this.in.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.in.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.in.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return this.in.skip(n);
    }

    @Override
    public int available() throws IOException {
        return this.in.available();
    }

    @Override
    public void mark(int readlimit) {
        this.in.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        this.in.reset();
    }

    @Override
    public boolean markSupported() {
        return this.in.markSupported();
    }
}
