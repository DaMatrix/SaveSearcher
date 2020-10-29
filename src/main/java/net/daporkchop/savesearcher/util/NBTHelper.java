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

package net.daporkchop.savesearcher.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.pool.handle.Handle;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.nbt.tag.Tag;
import net.daporkchop.lib.nbt.tag.notch.ByteArrayTag;
import net.daporkchop.lib.nbt.tag.notch.ByteTag;
import net.daporkchop.lib.nbt.tag.notch.CompoundTag;
import net.daporkchop.lib.nbt.tag.notch.DoubleTag;
import net.daporkchop.lib.nbt.tag.notch.FloatTag;
import net.daporkchop.lib.nbt.tag.notch.IntArrayTag;
import net.daporkchop.lib.nbt.tag.notch.IntTag;
import net.daporkchop.lib.nbt.tag.notch.ListTag;
import net.daporkchop.lib.nbt.tag.notch.LongArrayTag;
import net.daporkchop.lib.nbt.tag.notch.LongTag;
import net.daporkchop.lib.nbt.tag.notch.ShortTag;
import net.daporkchop.lib.nbt.tag.notch.StringTag;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class NBTHelper {
    public String toJson(@NonNull Tag nbt) {
        try (Handle<StringBuilder> handle = PorkUtil.STRINGBUILDER_POOL.get()) {
            StringBuilder builder = handle.value();
            builder.setLength(0);

            try (JsonWriter writer = new JsonWriter(Streams.writerForAppendable(builder))) {
                Streams.write(toJson0(nbt), writer);
            } catch (IOException e) {
                throw new AssertionError(e); //impossible
            }

            return builder.toString();
        }
    }

    private JsonElement toJson0(Tag nbt) {
        if (nbt instanceof CompoundTag) {
            JsonObject object = new JsonObject();
            ((CompoundTag) nbt).forEach((name, tag) -> object.add(name, toJson0(tag)));
            return object;
        } else if (nbt instanceof ListTag) {
            JsonArray array = new JsonArray();
            ((ListTag<? extends Tag>) nbt).forEach(tag -> array.add(toJson0(tag)));
            return array;
        } else if (nbt instanceof ByteTag) {
            return new JsonPrimitive(((ByteTag) nbt).getValue());
        } else if (nbt instanceof DoubleTag) {
            return new JsonPrimitive(((DoubleTag) nbt).getValue());
        } else if (nbt instanceof FloatTag) {
            return new JsonPrimitive(((FloatTag) nbt).getValue());
        } else if (nbt instanceof IntTag) {
            return new JsonPrimitive(((IntTag) nbt).getValue());
        } else if (nbt instanceof LongTag) {
            return new JsonPrimitive(((LongTag) nbt).getValue());
        } else if (nbt instanceof ShortTag) {
            return new JsonPrimitive(((ShortTag) nbt).getValue());
        } else if (nbt instanceof StringTag) {
            return new JsonPrimitive(((StringTag) nbt).getValue());
        } else if (nbt instanceof ByteArrayTag) {
            JsonArray array = new JsonArray();
            for (byte b : ((ByteArrayTag) nbt).value()) {
                array.add(b);
            }
            return array;
        } else if (nbt instanceof IntArrayTag) {
            JsonArray array = new JsonArray();
            for (int i : ((IntArrayTag) nbt).value()) {
                array.add(i);
            }
            return array;
        } else if (nbt instanceof LongArrayTag) {
            JsonArray array = new JsonArray();
            for (long l : ((LongArrayTag) nbt).getValue()) {
                array.add(l);
            }
            return array;
        } else {
            throw new IllegalArgumentException(PorkUtil.className(nbt));
        }
    }
}
