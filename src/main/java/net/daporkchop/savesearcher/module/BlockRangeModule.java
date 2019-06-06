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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.world.Column;

/**
 * @author DaPorkchop_
 */
public class BlockRangeModule extends BlockModule {
    protected int minY = 0;
    protected int maxY = 255;

    public BlockRangeModule(String[] args) {
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
                case "min":
                case "minY":{
                    this.minY = Integer.parseInt(split[1]);
                }
                break;
                case "max":
                case "maxY":{
                    this.maxY = Integer.parseInt(split[1]);
                }
                break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid argument: %s", s));
            }
        }
        if (this.searchName == null) {
            throw new IllegalArgumentException("No id given!");
        } else if (this.minY > this.maxY)   {
            throw new IllegalArgumentException(String.format("Min Y must be less than or equal to max Y! (min=%d, max=%d)", this.minY, this.maxY));
        }
    }

    @Override
    public void saveData(JsonObject object, Gson gson) {
        super.saveData(object, gson);
        JsonObject rangeObj = new JsonObject();
        rangeObj.addProperty("min", this.minY);
        rangeObj.addProperty("max", this.maxY);
        object.add("range", rangeObj);
    }

    @Override
    public void handle(long current, long estimatedTotal, Column column) {
        int maxY = this.maxY;
        int minY = this.minY; //allow JVM to inline into registers
        for (int x = 15; x >= 0; x--) {
            for (int z = 15; z >= 0; z--) {
                for (int y = maxY; y >= minY; y--) {
                    this.checkAndAddPos(x, y, z, column);
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Block - Ranged (id=%s, meta=%d, min=%d, max=%d)", this.searchName.toString(), this.meta, this.minY, this.maxY);
    }

    @Override
    public String getSaveFormat() {
        return "block_ranged";
    }
}
