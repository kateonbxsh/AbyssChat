package net.chatsystem.ui.panels.contact;

import net.chatsystem.models.User;
import net.chatsystem.ui.colors.Palette;
import net.chatsystem.ui.fonts.FontRepository;
import net.chatsystem.ui.system.Tray;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ContactItemPanel extends JPanel {

    private final User user;
    private final JLabel statusLabel;
    private final JLabel nameLabel;

    private int unreadCount = 0;
    private final JLabel unreadBadge;

    public boolean selected = false;

    public ContactItemPanel(User user) {
        this.user = user;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(Palette.CARD_BACK);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // slightly shorter height

        // ===== SQUARE ICON =====
        JPanel icon = new JPanel();
        icon.setPreferredSize(new Dimension(50, 50));
        icon.setMaximumSize(new Dimension(50, 50));
        icon.setBackground(Color.GRAY);
        icon.setAlignmentY(Component.CENTER_ALIGNMENT);

        // ===== TEXT PANEL =====
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBackground(new Color(0, 0, 0, 0));
        textPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        textPanel.setBorder(new EmptyBorder(0, 10, 0, 0)); // margin from icon

        nameLabel = new JLabel(this.user.getUsername());
        nameLabel.setFont(FontRepository.TextFont);
        nameLabel.setForeground(Color.WHITE);

        statusLabel = new JLabel(this.user.getStatus().toString());
        statusLabel.setFont(FontRepository.LabelFont.deriveFont(12f));
        statusLabel.setForeground(Palette.statusToColor(this.user.getStatus()));

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(nameLabel);
        textPanel.add(statusLabel);
        textPanel.add(Box.createVerticalGlue());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(Palette.CARD_BACK.brighter().brighter().brighter()); // hover color
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // hand cursor
            }

            @Override
            public void mouseExited(MouseEvent e) {
                resetBackground();
                setCursor(Cursor.getDefaultCursor()); // reset cursor
            }
        });

        unreadBadge = new JLabel();
        unreadBadge.setFont(FontRepository.LabelFont.deriveFont(Font.BOLD, 11f));
        unreadBadge.setForeground(Color.WHITE);
        unreadBadge.setBackground(new Color(220, 70, 70)); // red bubble
        unreadBadge.setOpaque(true);
        unreadBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
        unreadBadge.setVisible(false); // hidden by default
        unreadBadge.setAlignmentY(Component.CENTER_ALIGNMENT);

        add(icon);
        add(textPanel);
        add(Box.createHorizontalGlue()); // pushes content to the left
        add(unreadBadge);
    }

    public void resetBackground() {
        if (selected) setBackground(Palette.CARD_BACK.brighter().brighter());
        else setBackground(Palette.CARD_BACK);
    }

    public void update() {
        StringBuilder usernameBuilder = new StringBuilder();
        usernameBuilder.append(this.user.getUsername());
        boolean isMe = this.user.getAddress().equals(User.getInstance().getAddress());
        if (isMe) usernameBuilder.append(" (you)");
        nameLabel.setText(usernameBuilder.toString());
        statusLabel.setText(User.getStatusName(this.user.getStatus(), isMe));
        statusLabel.setFont(FontRepository.LabelFont.deriveFont(12f));
        statusLabel.setForeground(Palette.statusToColor(this.user.getStatus()));

    }

    public void setUsername(String username) {
        nameLabel.setText(username);
    }

    public void incrementUnread() {
        unreadCount++;

        unreadBadge.setText(unreadCount > 9 ? "9+" : String.valueOf(unreadCount));
        unreadBadge.setVisible(true);

        if (!User.getInstance().getStatus().equals(User.Status.DO_NOT_DISTURB)) Tray.sendMessageNotification(user);
    }

    public void clearUnread() {
        unreadCount = 0;
        unreadBadge.setVisible(false);
    }


}
