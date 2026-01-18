package net.chatsystem.ui.colors;

import net.chatsystem.models.User;

import java.awt.*;

public class Palette {

    public static Color MAIN = Color.decode("#ff4654");

    public static Color APP_BACK = Color.decode("#ba3a46");
    public static Color DEEP_BACK = Color.decode("#210606");

    public static Color CARD_BACK = Color.decode("#111823");

    public static Color FIELD_BACK = new Color(200, 200, 200, 255);
    public static Color FIELD_TEXT = Color.WHITE;

    public static Color statusToColor(User.Status status) {
        return switch (status) {
            case ONLINE -> Color.GREEN;
            case DO_NOT_DISTURB -> Color.RED;
            case AWAY -> Color.YELLOW;
            case OFFLINE -> Color.DARK_GRAY;
        };
    }

}
