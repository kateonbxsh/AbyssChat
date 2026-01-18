package net.chatsystem.ui;

import net.chatsystem.ui.colors.Palette;
import net.chatsystem.ui.panels.ClientPanel;
import net.chatsystem.ui.panels.EditProfilePanel;
import net.chatsystem.ui.panels.LoginPanel;
import net.chatsystem.ui.system.Tray;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public static class Page {
        public static final String LOGIN = "login";
        public static final String CHAT = "chat";
        public static final String PROFILE = "profile";
    }

    private static MainFrame instance;
    private final CardLayout cardLayout;
    private final JPanel cards;

    public static MainFrame getInstance() {
        return instance;
    }

    public MainFrame() {
        super("Abyss Chat");
        instance = this;

        Tray.setupTrayIcon();
        Tray.setupMinimizeToTray(this);

        UIManager.put("ScrollBar.thumb", Palette.MAIN);
        UIManager.put("ScrollBar.track", Palette.APP_BACK);
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("Button.disabledText", Color.WHITE);
        UIManager.put("Button.disabledBackground", Color.DARK_GRAY);

        this.setSize(1280, 720);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        LoginPanel login = new LoginPanel();
        cards.add(login, Page.LOGIN);
        ClientPanel client = new ClientPanel();
        cards.add(client, Page.CHAT);
        EditProfilePanel profile = new EditProfilePanel();
        cards.add(profile, Page.PROFILE);

        cards.setLayout(cardLayout);
        cardLayout.show(cards, Page.LOGIN);

        add(cards);
        this.setVisible(true);

    }

    public void show(String page) {
        cardLayout.show(cards, page);
    }

}
