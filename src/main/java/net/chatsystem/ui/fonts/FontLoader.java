package net.chatsystem.ui.fonts;

import java.awt.*;
import java.io.InputStream;

public class FontLoader {

    public static Font loadFont(String path, float size) {
        try (InputStream is = FontLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Font not found: " + path);
            }
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            return baseFont.deriveFont(size);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

