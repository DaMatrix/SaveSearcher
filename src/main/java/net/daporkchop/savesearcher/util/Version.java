/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2018-2020 DaPorkchop_ and contributors
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

package net.daporkchop.savesearcher.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.http.Http;
import net.daporkchop.lib.logging.Logging;
import net.daporkchop.savesearcher.Main;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.daporkchop.lib.logging.Logging.*;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class Version {
    public final String VERSION;

    static {
        try {
            JsonParser parser = new JsonParser();
            JsonObject local;
            try (Reader reader = new InputStreamReader(Main.class.getResourceAsStream("/version.json"))) {
                local = parser.parse(reader).getAsJsonObject();
            }
            try {
                JsonObject remote = parser.parse(Http.getString("https://raw.githubusercontent.com/DaMatrix/SaveSearcher/master/src/main/resources/version.json")).getAsJsonObject();
                int localVersion = toVersionNumber(local.get("nameNew").getAsString());
                int remoteVersion = toVersionNumber(remote.get("nameNew").getAsString());
                if (localVersion < remoteVersion) {
                    logger.alert(
                            "Outdated version! You're still on %s, but the latest version is %s.\nDownload the latest version from https://github.com/DaMatrix/SaveSearcher.\n\nScanner will start in 5 seconds...",
                            local.get("nameNew").getAsString().replaceAll(" ", ""),
                            remote.get("nameNew").getAsString().replaceAll(" ", "")
                    );

                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
                logger.warn("Version check failed, ignoring");
            }
            VERSION = local.get("nameNew").getAsString().replaceAll(" ", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int toVersionNumber(@NonNull String version)   {
        Matcher matcher = Pattern.compile("^ *?([0-9]+)\\. *?([0-9]+)\\. *?([0-9]+)$").matcher(version);
        if (!matcher.find())    {
            throw new IllegalArgumentException(version);
        }
        return Integer.parseInt(matcher.group(1)) * 1000000 + Integer.parseInt(matcher.group(2)) * 1000 + Integer.parseInt(matcher.group(3));
    }
}
