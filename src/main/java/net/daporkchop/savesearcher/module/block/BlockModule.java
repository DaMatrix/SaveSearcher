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
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.SearchModule;

/**
 * @author DaPorkchop_
 */
public class BlockModule extends SearchModule.BasePosSearchModule {
    protected ResourceLocation searchName;
    protected int meta = -1;
    protected int id;

    public BlockModule(String[] args) {
        for (String s : args) {
            if (s.isEmpty()) {
                continue;
            }
            String[] split = s.split("=");
            if (split.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
            switch (split[0]) {
                case "id": {
                    this.searchName = new ResourceLocation(split[1]);
                }
                break;
                case "meta": {
                    this.meta = Integer.parseInt(split[1]);
                    if (this.meta > 15 || this.meta < 0) {
                        throw new IllegalArgumentException(String.format("Invalid meta: %d (must be in range 0-15)", this.meta));
                    }
                }
                break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
        }
        if (this.searchName == null) {
            throw new IllegalArgumentException("No id given!");
        }
    }

    protected BlockModule() {
    }

    @Override
    public void init(World world) {
        this.id = world.getSave().registry(new ResourceLocation("minecraft:blocks")).lookup(this.searchName);
        if (this.id == -1) {
            throw new IllegalArgumentException(String.format("Invalid block id: %s", this.searchName.toString()));
        }
    }

    @Override
    public void saveData(JsonObject object) {
        super.saveData(object);
        object.addProperty("id", this.searchName.toString());
        object.addProperty("meta", this.meta);
    }

    @Override
    public void handle(long current, long estimatedTotal, Chunk chunk) {
        for (int x = 15; x >= 0; x--) {
            for (int z = 15; z >= 0; z--) {
                for (int y = 255; y >= 0; y--) {
                    this.checkAndAddPos(x, y, z, chunk);
                }
            }
        }
    }

    protected void checkAndAddPos(int x, int y, int z, Chunk chunk) {
        if (this.check(x, y, z, chunk)) {
            this.add(x + (chunk.getX() << 4), y, z + (chunk.getZ() << 4));
        }
    }

    protected boolean check(int x, int y, int z, Chunk chunk) {
        return chunk.getBlockId(x, y, z) == this.id && (this.meta == -1 || chunk.getBlockMeta(x, y, z) == this.meta);
    }

    @Override
    public String toString() {
        return String.format("Block (id=%s, meta=%d)", this.searchName.toString(), this.meta);
    }

    @Override
    public String getSaveName() {
        return "block";
    }

    @Override
    public int hashCode() {
        //id is only computed later and can change dynamically, so we don't want to include it in the hash code
        return this.searchName.hashCode() * 31 + this.meta;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj.getClass() == BlockModule.class) {
            //don't do instanceof check, since we only want to check if the modules are exactly identical
            BlockModule other = (BlockModule) obj;
            return this.searchName.equals(other.searchName) && this.meta == other.meta;
        } else {
            return false;
        }
    }
}
