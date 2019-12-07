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

package net.daporkchop.savesearcher.module.impl;

import com.google.gson.JsonObject;
import net.daporkchop.lib.minecraft.world.Chunk;
import net.daporkchop.lib.minecraft.world.World;
import net.daporkchop.lib.unsafe.PUnsafe;
import net.daporkchop.savesearcher.module.SearchModule;

/**
 * @author DaPorkchop_
 */
public final class AvgHeightModule implements SearchModule {
    protected static final long HEIGHT_OFFSET = PUnsafe.pork_getOffset(AvgHeightModule.class, "height");
    protected static final long COUNT_OFFSET  = PUnsafe.pork_getOffset(AvgHeightModule.class, "count");

    private volatile long height = 0L;
    private volatile long count  = 0L;

    public AvgHeightModule(String[] args) {
    }

    @Override
    public void init(World world) {
        this.height = this.count = 0L;
    }

    @Override
    public void saveData(JsonObject object) {
        object.addProperty("height", (double) this.height / (double) this.count);
    }

    @Override
    public void handle(long current, long estimatedTotal, Chunk chunk) {
        int c = 0;
        for (int x = 15; x >= 0; x--) {
            for (int z = 15; z >= 0; z--) {
                c += chunk.getHighestBlock(x, z);
            }
        }
        PUnsafe.getAndAddLong(this, HEIGHT_OFFSET, c);
        PUnsafe.getAndAddLong(this, COUNT_OFFSET, 256L);
    }

    @Override
    public String toString() {
        return "Average Height";
    }

    @Override
    public String getSaveName() {
        return "average_height";
    }

    @Override
    public int hashCode() {
        return AvgHeightModule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AvgHeightModule;
    }
}
