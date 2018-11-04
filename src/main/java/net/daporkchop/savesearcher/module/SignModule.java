/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2018-2018 DaPorkchop_ and contributors
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
import net.daporkchop.lib.minecraft.tileentity.TileEntitySign;
import net.daporkchop.lib.minecraft.world.Column;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.savesearcher.SearchModule;

/**
 * @author DaPorkchop_
 */
public class SignModule extends SearchModule.BasePosSearchModule {
    public SignModule(String[] args) {
    }

    @Override
    public void init(World world) {
    }

    @Override
    public void handle(long current, long estimatedTotal, Column column) {
        column.getTileEntities().stream()
                .filter(te -> te instanceof TileEntitySign)
                .map(te -> (TileEntitySign) te)
                .forEach(te -> this.add(te.getX(), te.getY(), te.getZ(), te.getLine1(), te.getLine2(), te.getLine3(), te.getLine4()));
    }

    @Override
    protected JsonObject getObject(int x, int y, int z, Object... args) {
        JsonObject object = super.getObject(x, y, z, args);
        object.addProperty("line1", (String) args[0]);
        object.addProperty("line2", (String) args[1]);
        object.addProperty("line3", (String) args[2]);
        object.addProperty("line4", (String) args[3]);
        return object;
    }

    @Override
    public String toString() {
        return "Signs";
    }

    @Override
    public String getSaveFormat() {
        return "sign";
    }
}
