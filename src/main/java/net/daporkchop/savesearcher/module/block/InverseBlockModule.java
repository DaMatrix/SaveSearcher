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

package net.daporkchop.savesearcher.module.block;

import com.google.gson.JsonObject;
import net.daporkchop.lib.minecraft.world.Column;

/**
 * @author DaPorkchop_
 */
public final class InverseBlockModule extends BlockModule {
    static JsonObject createElement(Column column) {
        JsonObject object = new JsonObject();
        object.addProperty("chunkX", column.getX());
        object.addProperty("chunkZ", column.getZ());
        return object;
    }

    public InverseBlockModule(String[] args) {
        super(args);
    }

    @Override
    public void handle(long current, long estimatedTotal, Column column) {
        for (int x = 15; x >= 0; x--) {
            for (int z = 15; z >= 0; z--) {
                for (int y = 255; y >= 0; y--) {
                    if (this.check(x, y, z, column)) {
                        return; //if block matches, break out
                    }
                }
            }
        }
        synchronized (this.values) {
            this.values.add(createElement(column));
        }
    }

    @Override
    public String toString() {
        return String.format("Block - Inverted (id=%s, meta=%d)", this.searchName.toString(), this.meta);
    }

    @Override
    public String getSaveName() {
        return "block_inverted";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj.getClass() == InverseBlockModule.class) {
            InverseBlockModule other = (InverseBlockModule) obj;
            return this.searchName.equals(other.searchName) && this.meta == other.meta;
        } else {
            return false;
        }
    }
}
