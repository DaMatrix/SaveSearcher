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

import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.daporkchop.lib.minecraft.entity.Entity;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.savesearcher.module.merging.AbstractEntityByIdSearchModule;
import net.daporkchop.savesearcher.output.OutputHandle;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = false)
final class EntityByIdModule extends AbstractEntityByIdSearchModule<EntityModule.EntityData> {
    protected final ResourceLocation filterId;

    public EntityByIdModule(@NonNull ResourceLocation filterId) {
        super(ImmutableSet.of(filterId));

        this.filterId = filterId;
    }

    @Override
    protected void processChunk(@NonNull Chunk chunk, @NonNull OutputHandle handle) {
        chunk.entities().forEach(entity -> {
            if (this.filterId.equals(entity.id())) {
                handle.accept(new EntityModule.EntityData(entity));
            }
        });
    }

    @Override
    protected void handleEntity(@NonNull Entity entity, @NonNull OutputHandle handle) {
        handle.accept(new EntityModule.EntityData(entity));
    }

    @Override
    public String toString() {
        return String.format("Entities (id=%s)", this.filterId);
    }
}
