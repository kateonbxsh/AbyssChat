package net.chatsystem.ui.fonts;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class FontRepository {

    public static final Font TitleFont = FontLoader.loadFont("/font/tungsten-bold.otf", 64f);
    public static Font ButtonFont;
    static {
        AffineTransform transform = new AffineTransform();
        transform.translate(0, 3);
        ButtonFont = FontLoader.loadFont("/font/tungsten-bold.otf", 26f).deriveFont(transform);
    }

    public static Font LabelFont = FontLoader.loadFont("/font/pfdin-bold.ttf", 18f);

    public static Font TextFont = FontLoader.loadFont("/font/pfdin-regular.ttf", 18f);

}
