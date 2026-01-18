package net.chatsystem.ui.util;

import net.chatsystem.ui.colors.Palette;
import net.chatsystem.ui.fonts.FontRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PanelUtilities {

    public static JTextField createField(String placeholder) {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setFont(FontRepository.TextFont);

        // Semi-transparent white
        field.setBackground(Palette.FIELD_TEXT);
        field.setForeground(Color.BLACK);

        field.setBorder(new LineBorder(Color.WHITE, 1));
        field.setToolTipText(placeholder);

        return field;
    }

    public static JButton createButton(String text) {
        JButton button = new JButton(text.toUpperCase());
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);

        button.setFont(FontRepository.ButtonFont.deriveFont(26f));
        button.setForeground(Color.WHITE);
        button.setBackground(Color.decode("#ff4654"));

        button.setBorder(new EmptyBorder(12, 24, 12, 24));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        button.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                if (!button.isEnabled()) return;
                button.setFont(FontRepository.ButtonFont.deriveFont(30f));
                button.setBorder(new EmptyBorder(10, 24, 10, 24));
                button.setForeground(Color.BLACK);
                button.setBackground(Color.decode("#fafafa"));
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                button.setForeground(Color.WHITE);
                button.setFont(FontRepository.ButtonFont.deriveFont(26f));
                button.setBackground(Color.decode("#ff4654"));
                button.setBorder(new EmptyBorder(12, 24, 12, 24));
                button.setCursor(Cursor.getDefaultCursor());
            }
        });

        return button;
    }

    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FontRepository.TextFont);

        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        label.setForeground(Color.WHITE);
        return label;
    }

}
