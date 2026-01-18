package net.chatsystem.ui.panels.chat;

import net.chatsystem.models.User;
import net.chatsystem.ui.fonts.FontRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MessageBubble extends JPanel {

    private final JLabel timeLabel;
    private final Instant timestamp;

    public MessageBubble(String text, boolean isOutgoing, Instant timestamp, User from) {
        setLayout(new FlowLayout(
                isOutgoing ? FlowLayout.RIGHT : FlowLayout.LEFT,
                6, 2
        ) {
            public Dimension preferredLayoutSize(Container target) {
                Dimension sd=super.preferredLayoutSize(target);
                sd.width=Math.min(200, sd.width);
                return sd;

            }
        });
        setOpaque(false);

        // Background panel (the "line")
        JPanel linePanel = new JPanel(new BorderLayout());
        linePanel.setBorder(new EmptyBorder(6, 10, 6, 10));
        linePanel.setBackground(new Color(255, 255, 255, 30)); // slight dark bg
        linePanel.setOpaque(true);

        // Username + message
        String name = isOutgoing ? "You" : from.getUsername();
        JLabel messageLabel = new JLabel(
                "<html><b>" + name + ":</b> " + text + "</html>"
        );
        messageLabel.setFont(FontRepository.TextFont.deriveFont(16f));
        messageLabel.setForeground(Color.WHITE);

        // Timestamp
        this.timestamp = timestamp;
        timeLabel = new JLabel(formatTime(timestamp));
        timeLabel.setFont(FontRepository.TextFont.deriveFont(12f));
        timeLabel.setForeground(new Color(200, 200, 200));
        timeLabel.setBorder(new EmptyBorder(0, 10, 0, 0));

        // Assemble
        linePanel.add(messageLabel, BorderLayout.WEST);
        linePanel.add(timeLabel, BorderLayout.EAST);

        add(linePanel, BorderLayout.CENTER);
    }

    private static final Duration JUST_NOW_THRESHOLD = Duration.ofSeconds(30);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm")
                    .withZone(ZoneId.systemDefault());

    public static String formatTime(Instant timestamp) {
        Instant now = Instant.now();

        if (timestamp.isAfter(now.minus(JUST_NOW_THRESHOLD))) {
            return "Just now";
        }

        return DATE_TIME_FORMATTER.format(timestamp);
    }

    public void updateTimestamps() {
        timeLabel.setText(formatTime(this.timestamp));
    }

}
