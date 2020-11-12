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

import lombok.NonNull;
import net.daporkchop.lib.minecraft.text.MCTextEncoder;
import net.daporkchop.lib.minecraft.text.MCTextType;
import net.daporkchop.lib.minecraft.text.parser.AutoMCFormatParser;
import net.daporkchop.lib.nbt.tag.CompoundTag;
import net.daporkchop.mcworldlib.block.BlockState;
import net.daporkchop.mcworldlib.registry.Registry;
import net.daporkchop.mcworldlib.save.Save;
import net.daporkchop.mcworldlib.util.Identifier;
import net.daporkchop.mcworldlib.world.World;
import net.daporkchop.mcworldlib.world.section.FlattenedSection;
import net.daporkchop.mcworldlib.world.section.LegacySection;
import net.daporkchop.mcworldlib.world.section.Section;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.PositionData;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
public final class SignModule extends AbstractSearchModule<SignModule.SignData> implements SearchModule.ForSection {
    private int standing_sign;
    private int wall_sign;

    public SignModule(String[] args) {
    }

    @Override
    public void init(@NonNull Save save, @NonNull OutputHandle<SignData> handle) {
        super.init(save, handle);

        Registry blockRegistry = save.version().registries().block();
        this.standing_sign = blockRegistry.get(Identifier.fromString("minecraft:standing_sign"));
        this.wall_sign = blockRegistry.get(Identifier.fromString("minecraft:wall_sign"));
    }

    @Override
    public void acceptChunk(@NonNull World world, @NonNull Section section) {
        section.tileEntities().forEach(te -> {
            if ("minecraft:sign".equals(te.getString("id"))) {
                this.handle.accept(new SignData(te, section));
            }
        });
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

        public SignData(@NonNull CompoundTag te, @NonNull Section section) {
            super(te);

            this.line1 = MCTextEncoder.encode(MCTextType.LEGACY, AutoMCFormatParser.DEFAULT.parse(te.getString("Text1")));
            this.line2 = MCTextEncoder.encode(MCTextType.LEGACY, AutoMCFormatParser.DEFAULT.parse(te.getString("Text2")));
            this.line3 = MCTextEncoder.encode(MCTextType.LEGACY, AutoMCFormatParser.DEFAULT.parse(te.getString("Text3")));
            this.line4 = MCTextEncoder.encode(MCTextType.LEGACY, AutoMCFormatParser.DEFAULT.parse(te.getString("Text4")));

            int direction = 0;
            String dir = null;
            String type = "invalid";

            if (section instanceof LegacySection) {
                int id = ((LegacySection) section).getCombinedIdMeta(this.x & 0xF, this.y & 0xF, this.z & 0xF);
                if ((id >> 4) == SignModule.this.standing_sign) {
                    type = "standing_sign";
                    direction = id & 0xF;
                } else if ((id >> 4) == SignModule.this.wall_sign) {
                    type = "wall_sign";
                    direction = ~(id & 0xF);
                }
            } else if (section instanceof FlattenedSection) {
                BlockState state = ((FlattenedSection) section).getBlockState(this.x & 0xF, this.y & 0xF, this.z & 0xF);
                if (state.id().name().endsWith("_wall_sign")) {
                    type = "wall_sign";
                    dir = state.properties().get("facing");
                } else if (state.id().name().endsWith("_sign")) {
                    type = "standing_sign";
                    direction = Integer.parseInt(state.properties().getOrDefault("rotation", "0"));
                }
            }

            if (dir == null) {
                dir = "unknown";
                if (direction >= 0) {
                    switch (direction) {
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
                } else {
                    switch (~direction) {
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
                }
            }
            this.direction = dir;
            this.type = type;
        }
    }
}
