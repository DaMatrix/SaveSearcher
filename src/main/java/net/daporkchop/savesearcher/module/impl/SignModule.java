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

package net.daporkchop.savesearcher.module.impl;

import com.google.gson.JsonObject;
import lombok.NonNull;
import net.daporkchop.lib.logging.format.FormatParser;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.text.parser.MinecraftFormatParser;
import net.daporkchop.lib.minecraft.tileentity.impl.TileEntitySign;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.PositionData;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
public final class SignModule extends AbstractSearchModule<SignModule.SignData> {
    private static final FormatParser PARSER = new MinecraftFormatParser();

    private int standing_sign;
    private int wall_sign;

    public SignModule(String[] args) {
    }

    @Override
    public void init(@NonNull World world, @NonNull OutputHandle handle) {
        super.init(world, handle);

        this.standing_sign = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(new ResourceLocation("minecraft:standing_sign"));
        this.wall_sign = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(new ResourceLocation("minecraft:wall_sign"));
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk, @NonNull OutputHandle handle) {
        chunk.tileEntities().stream()
                .filter(TileEntitySign.class::isInstance)
                .map(te -> new SignData((TileEntitySign) te, chunk))
                .forEach(handle::accept);
    }

    @Override
    public String toString() {
        return "Signs";
    }

    @Override
    public int hashCode() {
        return SignModule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SignModule;
    }

    protected final class SignData extends PositionData {
        public final String line1;
        public final String line2;
        public final String line3;
        public final String line4;

        public final String type;
        public final String direction;

        public SignData(@NonNull TileEntitySign te, @NonNull Chunk chunk) {
            super(te);

            this.line1 = PARSER.parse(te.line1()).toRawString();
            this.line2 = PARSER.parse(te.line2()).toRawString();
            this.line3 = PARSER.parse(te.line3()).toRawString();
            this.line4 = PARSER.parse(te.line4()).toRawString();

            int id = chunk.getBlockId(te.getX() & 0xF, te.getY(), te.getZ() & 0xF);
            int meta = chunk.getBlockMeta(te.getX() & 0xF, te.getY(), te.getZ() & 0xF);
            if (id == SignModule.this.standing_sign) {
                String dir = "unknown";
                switch (meta) {
                    case 0:
                        dir = "south";
                        break;
                    case 1:
                        dir = "south-southwest";
                        break;
                    case 2:
                        dir = "southwest";
                        break;
                    case 3:
                        dir = "west-southwest";
                        break;
                    case 4:
                        dir = "west";
                        break;
                    case 5:
                        dir = "west-northwest";
                        break;
                    case 6:
                        dir = "northwest";
                        break;
                    case 7:
                        dir = "north-northwest";
                        break;
                    case 8:
                        dir = "north";
                        break;
                    case 9:
                        dir = "north-northeast";
                        break;
                    case 10:
                        dir = "northeast";
                        break;
                    case 11:
                        dir = "east-northeast";
                        break;
                    case 12:
                        dir = "east";
                        break;
                    case 13:
                        dir = "east-southeast";
                        break;
                    case 14:
                        dir = "southeast";
                        break;
                    case 15:
                        dir = "south-southeast";
                        break;
                }
                this.type = "standing_sign";
                this.direction = dir;
            } else if (id == SignModule.this.wall_sign) {
                String dir = "unknown";
                switch (meta) {
                    case 2:
                        dir = "north";
                        break;
                    case 3:
                        dir = "south";
                        break;
                    case 4:
                        dir = "west";
                        break;
                    case 5:
                        dir = "east";
                        break;
                }
                this.type = "wall_sign";
                this.direction = dir;
            } else {
                this.type = String.format("invalid_id_%d", id);
                this.direction = "unknown";
            }
        }
    }
}
