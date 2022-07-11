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

package net.daporkchop.savesearcher.module.impl;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.logging.format.FormatParser;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.text.MCTextEncoder;
import net.daporkchop.lib.minecraft.text.MCTextType;
import net.daporkchop.lib.minecraft.text.parser.MinecraftFormatParser;
import net.daporkchop.lib.minecraft.tileentity.impl.TileEntitySign;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.PositionData;
import net.daporkchop.savesearcher.module.merging.AbstractTileEntityByClassSearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = false)
public final class SignModule extends AbstractTileEntityByClassSearchModule<SignModule.SignData, TileEntitySign> {
    private static final FormatParser PARSER = new MinecraftFormatParser();

    private final Mode mode;

    @EqualsAndHashCode.Exclude
    private int standing_sign;
    @EqualsAndHashCode.Exclude
    private int wall_sign;

    public SignModule(String[] args) {
        Mode mode = Mode.PLAIN_TEXT;

        for (String s : args) {
            String[] split = s.split("=");
            if (split.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
            switch (split[0]) {
                case "mode":
                    switch (split[1]) {
                        case "plain_text":
                            mode = Mode.PLAIN_TEXT;
                            break;
                        case "formatted_legacy":
                            mode = Mode.FORMATTED_LEGACY;
                            break;
                        case "raw":
                            mode = Mode.RAW;
                            break;
                        default:
                            throw new IllegalArgumentException(String.format("Invalid mode: %s", s));
                    }
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
        }

        this.mode = mode;
    }

    @Override
    public void init(@NonNull World world, @NonNull OutputHandle handle) {
        super.init(world, handle);

        this.standing_sign = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(new ResourceLocation("minecraft:standing_sign"));
        this.wall_sign = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(new ResourceLocation("minecraft:wall_sign"));
    }

    @Override
    protected void processTileEntity(@NonNull Chunk chunk, @NonNull TileEntitySign tileEntity) {
        this.handle.accept(new SignData(chunk, tileEntity, this.mode));
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        chunk.tileEntities().stream()
                .filter(TileEntitySign.class::isInstance)
                .map(te -> new SignData(chunk, (TileEntitySign) te, this.mode))
                .forEach(this.handle::accept);
    }

    @Override
    public String toString() {
        return String.format("Signs (mode=%s)", this.mode.name);
    }

    @RequiredArgsConstructor
    protected enum Mode {
        PLAIN_TEXT("Plain Text") {
            @Override
            public String parse(@NonNull String text) {
                return PARSER.parse(text).toRawString();
            }
        },
        FORMATTED_LEGACY("Legacy Formatting") {
            @Override
            public String parse(@NonNull String text) {
                return MCTextEncoder.encode(MCTextType.LEGACY, PARSER.parse(text));
            }
        },
        RAW("Raw") {
            @Override
            public String parse(@NonNull String text) {
                return text;
            }
        };

        @NonNull
        protected final String name;

        public abstract String parse(@NonNull String text);
    }

    protected final class SignData extends PositionData {
        public final String line1;
        public final String line2;
        public final String line3;
        public final String line4;

        public final String type;
        public final String direction;

        public SignData(@NonNull Chunk chunk, @NonNull TileEntitySign te, @NonNull Mode mode) {
            super(te);

            this.line1 = mode.parse(te.line1());
            this.line2 = mode.parse(te.line2());
            this.line3 = mode.parse(te.line3());
            this.line4 = mode.parse(te.line4());

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
