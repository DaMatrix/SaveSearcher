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
public final class TileEntitySpawner extends TileEntityBase {
    public static final ResourceLocation ID = new ResourceLocation("minecraft:mob_spawner");

    protected List<SpawnerEntry> entries = Collections.emptyList();

    @Override
    protected void doInit(@NonNull CompoundTag nbt) {
        if (nbt.contains("SpawnPotentials")) {
            this.entries = Collections.unmodifiableList(nbt.<CompoundTag>getList("SpawnPotentials").stream()
                    .map(tag -> new SpawnerEntry(new ResourceLocation(tag.getCompound("Entity").getString("id")), tag.getInt("Weight")))
                    .collect(Collectors.toList()));
        } else {
            this.entries = Collections.singletonList(new SpawnerEntry(
                    new ResourceLocation(nbt.getCompound("SpawnData").getString("id")),
                    1
            ));
        }
    }

    @Override
    protected void doDeinit() {
        this.entries = Collections.emptyList();
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    /**
     * Checks if this spawner is capable of spawning the given entity type.
     *
     * @param id the id of the entity
     * @return whether or not this spawner can spawn entities with the given id
     */
    public boolean canSpawn(@NonNull ResourceLocation id) {
        for (int i = 0, size = this.entries.size(); i < size; i++) {
            if (this.entries.get(i).id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * An entry representing the chance a mob has of being spawned by a spawner block.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    @Getter
    @Accessors(fluent = true)
    public static final class SpawnerEntry {
        protected final ResourceLocation id;
        protected final int              weight;
    }
}
