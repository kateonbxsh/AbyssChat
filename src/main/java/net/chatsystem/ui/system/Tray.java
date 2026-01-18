package net.chatsystem.ui.system;

import net.chatsystem.controller.CommandLine;
import net.chatsystem.models.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Tray {

    private static TrayIcon trayIcon;
    private static boolean isInTray = false;
    private static JFrame trackedWindow; // the window we minimize/restore

    // ===== Setup Tray Icon =====
    public static void setupTrayIcon() {

        if (!SystemTray.isSupported()) {
            CommandLine.error("System tray is not supported on this machine.");
            return;
        }


        SystemTray tray = SystemTray.getSystemTray();

        PopupMenu popup = new PopupMenu();

        // Add a "Close" menu item
        MenuItem app = new MenuItem("Abyss Chat");
        app.setEnabled(false);
        MenuItem closeItem = new MenuItem("Close app");
        closeItem.addActionListener(e -> {
            SystemTray.getSystemTray().remove(trayIcon);
            System.exit(0);
        });
        popup.add(app);
        popup.addSeparator();
        popup.add(closeItem);

        // Empty image as placeholder
        Image image = Toolkit.getDefaultToolkit().getImage(Tray.class.getResource("/icon.png"));

        trayIcon = new TrayIcon(image, "Abyss Chat", popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            CommandLine.error("AWT Exception thrown when adding tray icon: {}", e);
        }
    }

    // ===== Send notification =====
    public static void sendMessageNotification(User user) {
        if (trayIcon != null) {
            trayIcon.displayMessage(
                    "New message",
                    user.getUsername() + " sent you a message",
                    TrayIcon.MessageType.INFO
            );
        }
    }

    public static void setupMinimizeToTray(JFrame window) {
        if (!SystemTray.isSupported() || trayIcon == null || isInTray) return;

        trackedWindow = window;

        // Remove default close operation and replace with minimize
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                reduceToTray();
            }
        });

        // Add action to restore when tray icon clicked
        trayIcon.addActionListener(e -> restoreFromTray());
    }

    private static boolean firstClosing = true;

    private static void reduceToTray() {
        if (trackedWindow == null || isInTray) return;
        if (firstClosing) {
            firstClosing = false;
            if (trayIcon != null) {
                trayIcon.displayMessage(
                        "Minimized",
                        "AbyssChat is minimized",
                        TrayIcon.MessageType.INFO
                );
            }
        }
        trackedWindow.setVisible(false);
        isInTray = true;
    }

    // ===== Restore window from tray =====
    public static void restoreFromTray() {
        if (trackedWindow == null || !isInTray) return;

        trackedWindow.setVisible(true);
        trackedWindow.setState(Frame.NORMAL);
        trackedWindow.toFront();
        isInTray = false;
    }

    // ===== Check if window is in tray =====
    public static boolean isInTray() {
        return isInTray;
    }

}
