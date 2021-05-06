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

package net.daporkchop.savesearcher.tileentity;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.tileentity.TileEntityBase;
import net.daporkchop.lib.nbt.tag.notch.CompoundTag;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author DaPorkchop_
 */
@Getter
@Accessors(fluent = true)
public final class TileEntityCommandBlock extends TileEntityBase {
    public static final ResourceLocation ID = new ResourceLocation("minecraft:command_block");

    protected String command;
    protected String lastOutput;

    @Override
    protected void doInit(@NonNull CompoundTag nbt) {
        this.command = nbt.getString("Command", "");
        this.lastOutput = nbt.getString("LastOutput", "");
    }

    @Override
    protected void doDeinit() {
        this.command = null;
        this.lastOutput = null;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
