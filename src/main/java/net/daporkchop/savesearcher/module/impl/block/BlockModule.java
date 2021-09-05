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

package net.daporkchop.savesearcher.module.impl.block;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.util.PArrays;
import net.daporkchop.lib.math.vector.i.Vec3i;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.Section;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.lib.primitive.lambda.consumer.IntIntIntConsumer;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.module.merging.AbstractChunkSectionSearchModule;
import net.daporkchop.savesearcher.module.merging.AbstractEntityByIdSearchModule;
import net.daporkchop.savesearcher.module.merging.AbstractMergedSearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode
public final class BlockModule extends AbstractChunkSectionSearchModule<Vec3i> implements IntIntIntConsumer {
    public static SearchModule find(@NonNull String[] args) {
        ResourceLocation id = null;
        int meta = -1;
        int min = 0;
        int max = 255;
        boolean invert = false;
        boolean chunkinvert = false;

        for (String s : args) {
            String[] split = s.split("=");
            if (split.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
            switch (split[0]) {
                case "id":
                    id = new ResourceLocation(split[1]);
                    break;
                case "meta":
                    meta = Integer.parseInt(split[1]);
                    if (meta > 15 || meta < 0) {
                        throw new IllegalArgumentException(String.format("Invalid meta: %d (must be in range 0-15)", meta));
                    }
                    break;
                case "min":
                case "minY":
                    min = Integer.parseInt(split[1]);
                    break;
                case "max":
                case "maxY":
                    max = Integer.parseInt(split[1]);
                    break;
                case "invert":
                    if (chunkinvert) {
                        throw new IllegalArgumentException("invert and chunkinvert cannot be used together!");
                    }
                    invert = true;
                    break;
                case "chunkinvert":
                    if (invert) {
                        throw new IllegalArgumentException("invert and chunkinvert cannot be used together!");
                    }
                    chunkinvert = true;
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
        }

        if (id == null) {
            throw new IllegalArgumentException("No id given!");
        } else if (min > max) {
            throw new IllegalArgumentException(String.format("Min Y must be less than or equal to max Y! (min=%d, max=%d)", min, max));
        } else if (min == 0 && max == 255) {
            return chunkinvert
                    ? new ChunkInverseBlockModule(id, meta)
                    : invert ? new InverseBlockModule(id, meta) : new BlockModule(id, meta);
        } else {
            return chunkinvert
                    ? new ChunkInverseBlockRangeModule(id, meta, min, max)
                    : invert ? new InverseBlockRangeModule(id, meta, min, max) : new BlockRangeModule(id, meta, min, max);
        }
    }

    protected final ResourceLocation searchName;
    protected final int meta;

    @EqualsAndHashCode.Exclude
    protected int id;

    @Override
    public void init(@NonNull World world, @NonNull OutputHandle handle) {
        super.init(world, handle);

        if ((this.id = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(this.searchName)) == -1) {
            throw new IllegalArgumentException(String.format("Invalid block id: %s", this.searchName));
        }
    }

    @Override
    protected void processChunkSection(@NonNull Chunk chunk, @NonNull Section section) {
        final int id = this.id;
        final int meta = this.meta;

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    if (section.getBlockId(x, y, z) == id && (meta < 0 || section.getBlockMeta(x, y, z) == meta)) {
                        this.accept(chunk.minX() + x, (section.getY() << 4) + y, chunk.minZ() + z);
                    }
                }
            }
        }
    }

    @Override
    public void accept(int x, int y, int z) {
        this.handle.accept(new Vec3i(x, y, z));
    }

    @Override
    public String toString() {
        if (this.meta == -1) {
            return String.format("Block (id=%s)", this.searchName);
        } else {
            return String.format("Block (id=%s, meta=%d)", this.searchName, this.meta);
        }
    }

    @Override
    protected void mergeChunkSection(@NonNull List<AbstractChunkSectionSearchModule<?>> in, @NonNull Consumer<AbstractChunkSectionSearchModule<?>> addMerged) {
        if (in.stream().filter(BlockModule.class::isInstance).count() <= 2L) { //if there are less than 2 modules, the overhead of merging likely outweighs any potential benefits
            return;
        }

        List<BlockModule> modules = new ArrayList<>();
        for (Iterator<AbstractChunkSectionSearchModule<?>> itr = in.iterator(); itr.hasNext(); ) {
            AbstractChunkSectionSearchModule<?> module = itr.next();
            if (module instanceof BlockModule) {
                modules.add((BlockModule) module);
                itr.remove();
            }
        }

        IntObjectMap<List<IntIntIntConsumer>[]> initialIndex = new IntObjectHashMap<>();
        modules.forEach(module -> {
            List<IntIntIntConsumer>[] allMetas = initialIndex.computeIfAbsent(module.id, id -> uncheckedCast(PArrays.filled(16, List[]::new, (Supplier<List>) ArrayList::new)));
            if (module.meta < 0) {
                for (List<IntIntIntConsumer> list : allMetas) {
                    list.add(module);
                }
            } else {
                allMetas[module.meta].add(module);
            }
        });

        IntObjectMap<IntIntIntConsumer[]> compactIndex = new IntObjectHashMap<>();
        initialIndex.forEach((id, allMetas) -> compactIndex.put(id, Stream.of(allMetas)
                .map(list -> {
                    switch (list.size()) {
                        case 0:
                            return null;
                        case 1:
                            return list.get(0);
                        default:
                            IntIntIntConsumer[] callbacks = list.toArray(new IntIntIntConsumer[0]);
                            return (IntIntIntConsumer) (x, y, z) -> {
                                for (IntIntIntConsumer callback : callbacks) {
                                    callback.accept(x, y, z);
                                }
                            };
                    }
                })
                .toArray(IntIntIntConsumer[]::new)));

        addMerged.accept(new AbstractChunkSectionSearchModule.Merged(modules) {
            @Override
            protected void processChunkSection(@NonNull Chunk chunk, @NonNull Section section) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            IntIntIntConsumer[] allMetas = compactIndex.get(section.getBlockId(x, y, z));
                            if (allMetas != null) {
                                IntIntIntConsumer callback = allMetas[section.getBlockMeta(x, y, z)];
                                if (callback != null) {
                                    callback.accept(chunk.minX() + x, (section.getY() << 4) + y, chunk.minZ() + z);
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}
