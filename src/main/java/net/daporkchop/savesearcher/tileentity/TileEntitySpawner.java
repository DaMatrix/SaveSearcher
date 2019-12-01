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

package net.daporkchop.savesearcher.tileentity;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.tileentity.TileEntityBase;
import net.daporkchop.lib.nbt.tag.Tag;
import net.daporkchop.lib.nbt.tag.notch.CompoundTag;

import java.util.Collection;
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
        if (nbt.contains("SpawnPotentials"))     {
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
        super.doDeinit();
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    /**
     * An entry representing the chance a mob has of being spawned by a spawner block.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    @Getter
    @Accessors(fluent = true)
    public static final class SpawnerEntry  {
        protected final ResourceLocation id;
        protected final int weight;
    }
}
