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
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.tileentity.TileEntitySpawner;

/**
 * @author DaPorkchop_
 */
public final class SpawnerModule extends SearchModule.BasePosSearchModule {
    protected final ResourceLocation filterId;

    public SpawnerModule(String[] args) {
        switch (args.length)    {
            case 1:
                this.filterId = null;
                break;
            case 2:
                this.filterId = new ResourceLocation(args[1]);
                break;
            default:
                throw new IllegalArgumentException("--spawner must be called with either no arguments or the entity ID of the spawner type to search for!");
        }
    }

    @Override
    public void init(World world) {
    }

    @Override
    public void handle(long current, long estimatedTotal, Chunk chunk) {
        if (this.filterId == null) {
            chunk.tileEntities().stream()
                    .filter(TileEntitySpawner.class::isInstance)
                    .map(TileEntitySpawner.class::cast)
                    .forEach(te -> this.add(te.getX(), te.getY(), te.getZ(), chunk, te));
        } else {
            chunk.tileEntities().stream()
                    .filter(TileEntitySpawner.class::isInstance)
                    .map(TileEntitySpawner.class::cast)
                    .filter(te -> te.canSpawn(this.filterId))
                    .forEach(te -> this.add(te.getX(), te.getY(), te.getZ(), chunk, te));
        }
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
        return this.filterId == null ? "Spawners" : String.format("Spawners(%s)", this.filterId);
    }

    @Override
    public String getSaveName() {
        return "spawner";
    }

    @Override
    public int hashCode() {
        return this.filterId == null ? SpawnerModule.class.hashCode() : this.filterId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)    {
            return true;
        } else if (obj instanceof SpawnerModule)    {
            SpawnerModule other = (SpawnerModule) obj;
            if (this.filterId == null)  {
                return other.filterId == null;
            } else {
                return this.filterId.equals(other.filterId);
            }
        } else {
            return false;
        }
    }
}
