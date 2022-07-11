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

import lombok.NonNull;
import net.daporkchop.lib.common.function.io.IOConsumer;
import net.daporkchop.lib.math.vector.i.Vec2i;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.Section;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.PositionDataXZ;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;

/**
 * @author DaPorkchop_
 */
public final class BrokenLightingModule extends AbstractSearchModule<PositionDataXZ> {
    protected final String lightcleaner;

    public BrokenLightingModule(String[] args) {
       String lightcleaner = null;

        for (String s : args) {
            String[] split = s.split("=");
            if (split.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
            switch (split[0]) {
                case "lightcleaner":
                    lightcleaner = split[1];
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
        }

        this.lightcleaner = lightcleaner;
    }

    @Override
    public void init(@NonNull World world, @NonNull OutputHandle handle) {
        super.init(world, handle);

        if (this.lightcleaner != null) {
            this.handle = new OutputHandle() {
                final List<PositionDataXZ> positions = Collections.synchronizedList(new ArrayList<>());

                @Override
                public void init(@NonNull SearchModule module) {
                    handle.init(module);
                }

                @Override
                public void close() throws IOException {
                    handle.close();

                    try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(new FileOutputStream(BrokenLightingModule.this.lightcleaner))))) {
                        out.writeInt(-1);
                        out.writeByte(2);

                        Map<Vec2i, List<PositionDataXZ>> groupedByRegion = this.positions.stream().collect(Collectors.groupingBy(pos -> new Vec2i(pos.x >> 5, pos.z >> 5)));
                        out.writeInt(groupedByRegion.size());

                        for (List<PositionDataXZ> groupedPositions : groupedByRegion.values()) {
                            out.writeUTF("world");

                            out.writeInt(1);
                            out.writeInt(0);

                            Set<Vec2i> paddedUniquePositions = groupedPositions.stream()
                                    .flatMap(pos -> {
                                        final int r = 1;
                                        List<Vec2i> padding = new ArrayList<>((r * 2 + 1) * (r * 2 + 1));
                                        for (int dx = -r; dx <= r; dx++) {
                                            for (int dz = -r; dz <= r; dz++) {
                                                padding.add(new Vec2i(pos.x + dx, pos.z + dz));
                                            }
                                        }
                                        return padding.stream();
                                    })
                                    .collect(Collectors.toSet());

                            out.writeInt(paddedUniquePositions.size());
                            paddedUniquePositions.forEach((IOConsumer<Vec2i>) v -> out.writeLong(((long) v.getX() << 32) + v.getY() - Integer.MIN_VALUE));
                        }
                    }
                }

                @Override
                public void accept(@NonNull Object data) {
                    handle.accept(data);

                    this.positions.add((PositionDataXZ) data);
                }
            };
        }
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        for (int z = 0; z < 16; z++) {
            X:
            for (int x = 0; x < 16; x++) {
                for (int chunkY = 15; chunkY >= 0; chunkY--) {
                    final Section section = chunk.section(chunkY);
                    if (section == null) {
                        continue;
                    }

                    for (int y = 15; y >= 0; y--) {
                        if (section.getBlockId(x, y, z) != 0) {
                            int blockY = (chunkY << 4) | y;
                            if (blockY != 255 && chunk.getSkyLight(x, blockY + 1, z) != 15) {
                                this.handle.accept(new PositionDataXZ(chunk.pos()));
                                return;
                            }
                            continue X;
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Broken Lighting";
    }

    @Override
    public int hashCode() {
        return BrokenLightingModule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BrokenLightingModule;
    }
}
