package org.luke.jwin.icons;

import org.luke.gui.exception.ErrorHandler;
import org.luke.jwin.app.Command;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
            ErrorHandler.handle(e1, "convert png to ico");
            Thread.currentThread().interrupt();
        }
        return null;
    }

    private static File getMagick() {
        return new File(URLDecoder.decode(Objects.requireNonNull(IconUtils.class.getResource("/magick.exe"))
                .getFile(), StandardCharsets.UTF_8))
                .getParentFile();
    }
}
