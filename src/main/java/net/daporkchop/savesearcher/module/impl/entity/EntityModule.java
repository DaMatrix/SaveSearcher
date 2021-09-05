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

package net.daporkchop.savesearcher.module.impl.entity;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.minecraft.entity.Entity;
import net.daporkchop.lib.minecraft.entity.impl.UnknownEntity;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.savesearcher.module.AbstractSearchModule;
import net.daporkchop.savesearcher.module.SearchModule;
import net.daporkchop.savesearcher.util.NBTHelper;

/**
 * @author DaPorkchop_
 */
public final class EntityModule extends AbstractSearchModule<EntityModule.EntityData> {
    public static SearchModule find(@NonNull String[] args) {
        switch (args.length) {
            case 0:
                return new EntityModule();
            case 1:
                return new EntityByIdModule(new ResourceLocation(args[0]));
            default:
                throw new IllegalArgumentException("--entity must be called with either no arguments or the entity ID to search for!");
        }
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk) {
        chunk.entities().forEach(entity -> this.handle.accept(new EntityData(entity)));
    }

    @Override
    public String toString() {
        return "Entities";
    }

    @Override
    public int hashCode() {
        return EntityModule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntityModule;
    }

    @RequiredArgsConstructor
    protected static final class EntityData {
        @NonNull
        public final ResourceLocation id;
        public final double x;
        public final double y;
        public final double z;
        @NonNull
        public final String nbt;

        public EntityData(@NonNull Entity entity) {
            this(entity.id(), entity.getX(), entity.getY(), entity.getZ(),
                    entity instanceof UnknownEntity ? NBTHelper.toJson(((UnknownEntity) entity).data()) : "{}");
        }
    }
}
