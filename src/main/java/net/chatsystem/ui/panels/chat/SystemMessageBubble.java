package net.chatsystem.ui.panels.chat;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SystemMessageBubble extends JPanel {

    public SystemMessageBubble(String text) {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setOpaque(false);

        JLabel label = new JLabel(text);
        label.setForeground(Color.GRAY);
        label.setFont(label.getFont().deriveFont(Font.ITALIC, 13f));
        label.setBorder(new EmptyBorder(6, 12, 6, 12));

        add(label);
    }
}