package org.luke.jwin.icons;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class IconUtils {
    public static File toIco(File in) {
        try {
            File out = new File(in.getAbsolutePath().replace(".png", ".ico"));

            new Command("cmd", "/c",
                    "magick \"" + in.getAbsolutePath()
                            + "\" -define icon:auto-resize=256,128,96,70,64,48,32,16 \""
                            + out.getAbsolutePath() + "\"")
                    .execute(getMagick()).waitFor();

            return out;
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return null;
    }

    private static File getMagick() {
        return new File(URLDecoder.decode(IconUtils.class.getResource("/magick.exe").getFile(), StandardCharsets.UTF_8))
                .getParentFile();
    }
}
