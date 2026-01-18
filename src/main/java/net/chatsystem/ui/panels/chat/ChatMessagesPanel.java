package net.chatsystem.ui.panels.chat;

import net.chatsystem.models.User;
import net.chatsystem.ui.colors.Palette;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

class ChatMessagesPanel extends JPanel {

    private final JPanel messagesPanel;
    private final JScrollPane messageScroll;

    public ChatMessagesPanel(User user) {

        setLayout(new BorderLayout());
        setOpaque(false);

        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setOpaque(false);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(Palette.DEEP_BACK);
        wrapperPanel.add(messagesPanel, BorderLayout.SOUTH);

        messageScroll = new JScrollPane(wrapperPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messageScroll.setBorder(null);
        messageScroll.getVerticalScrollBar().setUnitIncrement(12);

        startLabel = new SystemMessageBubble(
                "This is the start of your chat with " + user.getUsername()
        );
        messagesPanel.add(startLabel);
        messagesPanel.add(Box.createVerticalStrut(8));

        add(wrapperPanel, BorderLayout.CENTER);
    }

    private final SystemMessageBubble startLabel;
    private Instant lastMessageTime;

    public void addMessage(String text, boolean isMe, Instant timestamp, User from) {
        updateTimestamps();

        if (lastMessageTime == null ||
                !isSameDay(lastMessageTime, timestamp)) {
            addDaySeparator(timestamp);
            lastMessageTime = timestamp;
        }

        messagesPanel.add(new MessageBubble(text, isMe, timestamp, from));
        messagesPanel.add(Box.createVerticalStrut(2));
        messagesPanel.revalidate();
        messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> messageScroll.getVerticalScrollBar()
                .setValue(messageScroll.getVerticalScrollBar().getMaximum()));

        startLabel.setVisible(false);
    }

    public void updateTimestamps() {
        for (Component p : messagesPanel.getComponents()) {
            if (p instanceof MessageBubble bubble) bubble.updateTimestamps();
        }
    }

    private boolean isSameDay(Instant a, Instant b) {
        return a.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                .equals(b.atZone(java.time.ZoneId.systemDefault()).toLocalDate());
    }

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy");

    private static final DateTimeFormatter DAY_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE");


    private void addDaySeparator(Instant timestamp) {
        LocalDate messageDay = timestamp
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate today = LocalDate.now();
        String label;

        if (messageDay.equals(today)) {
            label = "Today";
        }
        else if (messageDay.equals(today.minusDays(1))) {
            label = "Yesterday";
        }
        else if (isSameWeek(messageDay, today)) {
            label = DAY_FORMATTER.format(messageDay); // Monday, Tuesday, etc.
        }
        else {
            label = DATE_FORMATTER.format(messageDay);
        }

        messagesPanel.add(new SystemMessageBubble(label));
        messagesPanel.add(Box.createVerticalStrut(8));
    }

    private boolean isSameWeek(LocalDate a, LocalDate b) {
        WeekFields wf = WeekFields.of(Locale.getDefault());
        return a.get(wf.weekOfWeekBasedYear()) == b.get(wf.weekOfWeekBasedYear())
                && a.getYear() == b.getYear();
    }



}
