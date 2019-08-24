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

package net.daporkchop.savesearcher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.daporkchop.lib.math.vector.i.Vec3i;
import net.daporkchop.lib.minecraft.region.WorldScanner;
import net.daporkchop.lib.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author DaPorkchop_
 */
public interface SearchModule extends WorldScanner.ColumnProcessor {
    void init(World world);

    void saveData(JsonObject object, Gson gson);

    String getSaveName();

    default Collection<Vec3i> getLocations()    {
        return null;
    }

    default void beforeExit(Collection<SearchModule> modules, Gson gson, World world)  {
    }

    abstract class BasePosSearchModule implements SearchModule  {
        protected final JsonArray values = new JsonArray();

        @Override
        public void saveData(JsonObject object, Gson gson) {
            object.add("values", this.values);
        }

        protected void add(int x, int y, int z, Object... args) {
            JsonObject object = this.getObject(x, y, z, args);
            synchronized (this.values)  {
                this.values.add(object);
            }
        }

        protected JsonObject getObject(int x, int y, int z, Object... args) {
            JsonObject object = new JsonObject();
            object.addProperty("x", x);
            object.addProperty("y", y);
            object.addProperty("z", z);
            return object;
        }

        @Override
        public Collection<Vec3i> getLocations() {
            return StreamSupport.stream(this.values.spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(o -> new Vec3i(
                            o.get("x").getAsInt(),
                            o.get("y").getAsInt(),
                            o.get("z").getAsInt()
                    ))
                    .collect(Collectors.toCollection(ArrayDeque::new));
        }
    }
}
