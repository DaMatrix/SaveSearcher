/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2018-2020 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.daporkchop.savesearcher.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class Version {
    public final String VERSION;

    static {
        VERSION = "unknown";
        /*try {
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
        }*/
    }

    private int toVersionNumber(@NonNull String version) {
        Matcher matcher = Pattern.compile("^ *?([0-9]+)\\. *?([0-9]+)\\. *?([0-9]+)$").matcher(version);
        if (!matcher.find()) {
            throw new IllegalArgumentException(version);
        }
        return Integer.parseInt(matcher.group(1)) * 1000000 + Integer.parseInt(matcher.group(2)) * 1000 + Integer.parseInt(matcher.group(3));
    }
}
