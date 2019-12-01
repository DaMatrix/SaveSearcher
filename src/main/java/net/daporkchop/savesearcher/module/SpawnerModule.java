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
import com.google.gson.JsonObject;
import net.daporkchop.lib.logging.format.FormatParser;
import net.daporkchop.lib.minecraft.text.parser.MinecraftFormatParser;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.SearchModule;
import net.daporkchop.savesearcher.tileentity.TileEntitySpawner;

/**
 * @author DaPorkchop_
 */
public final class SpawnerModule extends SearchModule.BasePosSearchModule {
    public SpawnerModule(String[] args) {
    }

    @Override
    public void init(World world) {
    }

    @Override
    public void handle(long current, long estimatedTotal, Chunk chunk) {
        chunk.tileEntities().stream()
                .filter(TileEntitySpawner.class::isInstance)
                .map(TileEntitySpawner.class::cast)
                .forEach(te -> this.add(te.getX(), te.getY(), te.getZ(), chunk, te));
    }

    @Override
    protected JsonObject getObject(int x, int y, int z, Object... args) {
        JsonObject object = super.getObject(x, y, z, args);

        object.add(
                "spawn_potentials",
                ((TileEntitySpawner) args[1]).entries().stream()
                        .map(entry -> {
                            JsonObject o = new JsonObject();
                            o.addProperty("id", entry.id().toString());
                            o.addProperty("weight", entry.weight());
                            return o;
                        })
                        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll)
        );

        return object;
    }

    @Override
    public String toString() {
        return "Spawners";
    }

    @Override
    public String getSaveName() {
        return "spawner";
    }

    @Override
    public int hashCode() {
        return SpawnerModule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SpawnerModule;
    }
}
