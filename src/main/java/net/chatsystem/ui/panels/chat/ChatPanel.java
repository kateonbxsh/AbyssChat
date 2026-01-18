package net.chatsystem.ui.panels.chat;

import net.chatsystem.models.Contact;
import net.chatsystem.models.User;
import net.chatsystem.network.chat.ChatServer;
import net.chatsystem.network.chat.ChatSession;
import net.chatsystem.network.exceptions.RecipientOfflineException;
import net.chatsystem.observer.IObserver;
import net.chatsystem.ui.colors.Palette;
import net.chatsystem.ui.util.PanelUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ChatPanel extends JPanel implements IObserver {

    private static final String EMPTY_CARD = "__EMPTY__";

    private static ChatPanel instance;
    public static ChatPanel getInstance() {
        return instance;
    }

    private final JPanel cardContainer;
    private final CardLayout cardLayout;
    private final Map<InetAddress, ChatCard> contactCards = new HashMap<>();
    private Contact chattingWith;

    public Contact getChattingWith() {
        return chattingWith;
    }

    public ChatPanel() {
        instance = this;

        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(Color.DARK_GRAY);

        add(cardContainer, BorderLayout.CENTER);

        cardContainer.add(createEmptyCard(), EMPTY_CARD);
        cardLayout.show(cardContainer, EMPTY_CARD);
        cardContainer.revalidate();

        // Register as observer
        ChatServer.getInstance().addObserver(this);
    }

    public void toggleChat(Contact contact) {
        if (chattingWith != null && chattingWith.equals(contact)) {
            // hide
            chattingWith = null;
            cardLayout.show(cardContainer, EMPTY_CARD);
            cardContainer.revalidate();
            return;
        }
        getOrCreateCard(contact).messagesPanel.updateTimestamps();
        cardLayout.show(cardContainer, contact.getAddress().toString());
        cardContainer.revalidate();
        chattingWith = contact;
    }

    private ChatCard getOrCreateCard(Contact contact) {
        InetAddress ip = contact.getAddress();

        ChatCard card = contactCards.get(ip);
        if (card == null) {
            card = new ChatCard(contact);
            contactCards.put(ip, card);
            cardContainer.add(card, ip.toString());
        }
        return card;

    }

    // ===== Observer callbacks =====
    @Override
    public void onChatMessage(Contact from, String chat) {
        SwingUtilities.invokeLater(() -> {
            getOrCreateCard(from).messagesPanel.addMessage(chat, false, Instant.now(), from);
        });
    }

    @Override
    public void onDiscoverContact(Contact contact) {
        getOrCreateCard(contact);
    }

    private JPanel createEmptyCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Palette.DEEP_BACK);

        JLabel label = new JLabel("Click on a contact to chat");
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(label.getFont().deriveFont(Font.ITALIC, 16f));

        panel.add(label);
        return panel;
    }


    // ===== Inner class for each contact's card =====
    private static class ChatCard extends JPanel {

        private final ChatMessagesPanel messagesPanel;
        private final JTextField inputField;
        private final ChatSession chat;

        public ChatCard(Contact contact) {
            setLayout(new BorderLayout());
            setBackground(Palette.DEEP_BACK);

            this.chat = new ChatSession(contact);

            messagesPanel = new ChatMessagesPanel(contact);
            add(messagesPanel, BorderLayout.CENTER);

            JPanel inputPanel = new JPanel(new BorderLayout(6, 6));
            inputPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
            inputPanel.setBackground(Color.GRAY);

            inputField = PanelUtilities.createField("Type a message");
            inputField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            inputField.setBackground(Palette.FIELD_TEXT);
            inputField.setForeground(Color.BLACK);
            inputField.addActionListener(e -> sendMessage());

            JButton sendButton = PanelUtilities.createButton("SEND");
            sendButton.setPreferredSize(new Dimension(90, 40));
            sendButton.addActionListener(e -> sendMessage());

            inputPanel.add(inputField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);
            add(inputPanel, BorderLayout.SOUTH);
        }

        private void sendMessage() {
            String text = inputField.getText().trim();
            if (text.isEmpty()) return;

            try {
                // Attempt to open chat only when sending
                if (!chat.isOpen()) {
                    chat.attemptOpen();
                }

                chat.send(text);

                messagesPanel.addMessage(
                        text,
                        true,
                        Instant.now(),
                        User.getInstance()
                );

                inputField.setText("");

            } catch (RecipientOfflineException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "User is currently offline.",
                        "Cannot send message",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to send message.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

    }
}
