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

package net.daporkchop.savesearcher.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NonNull;
import net.daporkchop.lib.math.vector.i.Vec3i;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.Column;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.SearchModule;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author DaPorkchop_
 */
public final class NetherChunksModule implements SearchModule {
    protected final JsonArray values = new JsonArray();
    protected int bedrock_id;

    public NetherChunksModule(String[] args) {
    }

    @Override
    public void init(World world) {
        this.bedrock_id = world.getSave().getRegistry(new ResourceLocation("minecraft:blocks")).getId(new ResourceLocation("minecraft:bedrock"));
    }

    @Override
    public void handle(long current, long estimatedTotal, @NonNull Column column) {
        final int id = this.bedrock_id;
        final Chunk chunk = column.getChunk(7);
        if (chunk == null)  {
            return;
        }
        for (int x = 15; x >= 0; x--)   {
            for (int z = 15; z >= 0; z--)   {
                if (chunk.getBlockId(x, 15, z) == id)   {
                    this.add(column);
                    return;
                }
            }
        }
    }

    protected void add(@NonNull Column column)  {
        JsonObject obj = new JsonObject();
        obj.addProperty("x", column.getX());
        obj.addProperty("z", column.getZ());
        this.values.add(obj);
    }

    @Override
    public Collection<Vec3i> getLocations() {
        return StreamSupport.stream(this.values.spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(o -> new Vec3i(
                        o.get("x").getAsInt() << 4,
                        128,
                        o.get("z").getAsInt() << 4
                ))
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    @Override
    public void saveData(JsonObject object) {
        object.add("chunks", this.values);
    }

    @Override
    public String getSaveName() {
        return "nether_chunks";
    }

    @Override
    public String toString() {
        return "Nether Chunks";
    }

    @Override
    public int hashCode() {
        return NetherChunksModule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NetherChunksModule;
    }
}
