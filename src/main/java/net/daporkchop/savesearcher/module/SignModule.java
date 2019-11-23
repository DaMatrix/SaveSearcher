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

import com.google.gson.JsonObject;
import net.daporkchop.lib.minecraft.registry.ResourceLocation;
import net.daporkchop.lib.minecraft.tileentity.TileEntitySign;
import net.daporkchop.lib.minecraft.world.Column;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.SearchModule;

/**
 * @author DaPorkchop_
 */
public class SignModule extends SearchModule.BasePosSearchModule {
    protected int standing_sign;
    protected int wall_sign;

    public SignModule(String[] args) {
    }

    @Override
    public void init(World world) {
        this.standing_sign = world.getSave().getRegistry(new ResourceLocation("minecraft:blocks")).getId(new ResourceLocation("minecraft:standing_sign"));
        this.wall_sign = world.getSave().getRegistry(new ResourceLocation("minecraft:blocks")).getId(new ResourceLocation("minecraft:wall_sign"));
    }

    @Override
    public void handle(long current, long estimatedTotal, Column column) {
        column.getTileEntities().stream()
                .filter(TileEntitySign.class::isInstance)
                .map(TileEntitySign.class::cast)
                .forEach(te -> this.add(te.getX(), te.getY(), te.getZ(), column, te.getLine1(), te.getLine2(), te.getLine3(), te.getLine4()));
    }

    @Override
    protected JsonObject getObject(int x, int y, int z, Object... args) {
        JsonObject object = super.getObject(x, y, z, args);

        object.addProperty("line1", (String) args[1]);
        object.addProperty("line2", (String) args[2]);
        object.addProperty("line3", (String) args[3]);
        object.addProperty("line4", (String) args[4]);

        int id = ((Column) args[0]).getBlockId(x & 0xF, y & 0xF, z & 0xF);
        int meta = ((Column) args[0]).getBlockMeta(x & 0xF, y & 0xF, z & 0xF);
        if (id == this.standing_sign)   {
            object.addProperty("type", "standing_sign");
            String dir = "unknown";
            switch (meta)   {
                case 0:
                    dir = "south";
                    break;
                case 1:
                    dir = "south-southwest";
                    break;
                case 2:
                    dir = "southwest";
                    break;
                case 3:
                    dir = "west-southwest";
                    break;
                case 4:
                    dir = "west";
                    break;
                case 5:
                    dir = "west-northwest";
                    break;
                case 6:
                    dir = "northwest";
                    break;
                case 7:
                    dir = "north-northwest";
                    break;
                case 8:
                    dir = "north";
                    break;
                case 9:
                    dir = "north-northeast";
                    break;
                case 10:
                    dir = "northeast";
                    break;
                case 11:
                    dir = "east-northeast";
                    break;
                case 12:
                    dir = "east";
                    break;
                case 13:
                    dir = "east-southeast";
                    break;
                case 14:
                    dir = "southeast";
                    break;
                case 15:
                    dir = "south-southeast";
                    break;
            }
            object.addProperty("direction", dir);
        } else if (id == this.wall_sign)    {
            object.addProperty("type", "wall_sign");
            String dir = "unknown";
            switch (meta)   {
                case 2:
                    dir = "north";
                    break;
                case 3:
                    dir = "south";
                    break;
                case 4:
                    dir = "west";
                    break;
                case 5:
                    dir = "east";
                    break;
            }
            object.addProperty("direction", dir);
        } else {
            object.addProperty("type", String.format("invalid_id_%d", id));
            object.addProperty("direction", "unknown");
        }

        return object;
    }

    @Override
    public String toString() {
        return "Signs";
    }

    @Override
    public String getSaveName() {
        return "sign";
    }
}
