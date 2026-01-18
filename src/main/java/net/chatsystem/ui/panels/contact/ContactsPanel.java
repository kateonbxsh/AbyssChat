package net.chatsystem.ui.panels.contact;

import net.chatsystem.models.Contact;
import net.chatsystem.models.ContactList;
import net.chatsystem.models.User;
import net.chatsystem.network.chat.ChatServer;
import net.chatsystem.network.discovery.DiscoveryServer;
import net.chatsystem.observer.IObserver;
import net.chatsystem.ui.MainFrame;
import net.chatsystem.ui.colors.Palette;
import net.chatsystem.ui.fonts.FontRepository;
import net.chatsystem.ui.panels.chat.ChatPanel;
import net.chatsystem.ui.system.Tray;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class ContactsPanel extends JPanel implements IObserver {

    private final ContactItemPanel meCard;
    private final JPanel contactsListPanel;

    // map UUID -> ContactItemPanel
    private final Map<InetAddress, ContactItemPanel> contactPanels = new HashMap<>();

    public ContactsPanel() {

        setLayout(new BorderLayout());
        setBackground(Palette.APP_BACK);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ───────────────
        // TOP (YOU + CARD)
        // ───────────────
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Palette.APP_BACK);

        JLabel meHeader = new JLabel("YOU");
        meHeader.setFont(FontRepository.TitleFont.deriveFont(24f));
        meHeader.setForeground(Color.WHITE);
        meHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
        meHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(meHeader);

        meCard = new ContactItemPanel(User.getInstance());
        meCard.setBorder(new EmptyBorder(4, 4, 4, 4));
        meCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        meCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                MainFrame.getInstance().show(MainFrame.Page.PROFILE);
            }
        });
        topPanel.add(meCard);

        add(topPanel, BorderLayout.NORTH);

        // ─────────────────────────
        // CENTER (CONTACTS SECTION)
        // ─────────────────────────
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Palette.APP_BACK);

        JLabel contactsHeader = new JLabel("YOUR CONTACTS");
        contactsHeader.setFont(FontRepository.TitleFont.deriveFont(24f));
        contactsHeader.setForeground(Color.WHITE);
        contactsHeader.setBorder(new EmptyBorder(16, 0, 10, 0));
        centerPanel.add(contactsHeader, BorderLayout.NORTH);

        // Scrollable list
        contactsListPanel = new JPanel();
        contactsListPanel.setLayout(new BoxLayout(contactsListPanel, BoxLayout.Y_AXIS));
        contactsListPanel.setBackground(Palette.APP_BACK);

        for (Contact c : ContactList.getInstance().getContacts()) {
            addOrUpdateContactPanel(c);
        }

        JScrollPane contactScroll = new JScrollPane(contactsListPanel);
        contactScroll.setBorder(null);
        contactScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        contactScroll.getVerticalScrollBar().setUnitIncrement(12);
        styleScrollBar(contactScroll.getVerticalScrollBar());

        centerPanel.add(contactScroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        DiscoveryServer.getInstance().addObserver(this);
        ChatServer.getInstance().addObserver(this);
    }

    private ContactItemPanel chosenContactPanel;

    private void addOrUpdateContactPanel(Contact contact) {

        if (contactPanels.containsKey(contact.getAddress())) {
            ContactItemPanel panel = contactPanels.get(contact.getAddress());
            panel.update();
            panel.revalidate();
            panel.repaint();
            return;
        }

        ContactItemPanel panel = createContactItemPanel(contact);

        contactPanels.put(contact.getAddress(), panel);
        contactsListPanel.add(panel);
        contactsListPanel.add(Box.createVerticalStrut(8));
        contactsListPanel.revalidate();
        contactsListPanel.repaint();
    }

    private ContactItemPanel getOrCreateContactItemPanel(Contact contact) {
        if (contactPanels.containsKey(contact.getAddress())) {
            return contactPanels.get(contact.getAddress());
        }
        return createContactItemPanel(contact);
    }

    private ContactItemPanel createContactItemPanel(Contact contact) {
        ContactItemPanel panel = new ContactItemPanel(contact);
        panel.setBorder(new EmptyBorder(4,4,4,4));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ChatPanel.getInstance().toggleChat(contact);
                if (chosenContactPanel != null) {
                    chosenContactPanel.selected = false;
                    panel.selected = false;
                    chosenContactPanel.resetBackground();
                }
                if (panel.selected) {
                    chosenContactPanel = null;
                    panel.selected = false;
                } else {
                    chosenContactPanel = panel;
                    panel.selected = true;
                }
                panel.resetBackground();
                panel.clearUnread();
            }
        });
        return panel;
    }

    private void styleScrollBar(JScrollBar scrollBar) {
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = Palette.MAIN.darker();
                trackColor = Palette.APP_BACK.darker();
            }

            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }

            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }

            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0,0));
                btn.setMinimumSize(new Dimension(0,0));
                btn.setMaximumSize(new Dimension(0,0));
                return btn;
            }
        });
    }

    @Override
    public void onChatMessage(Contact from, String chat) {
        ContactItemPanel panel = getOrCreateContactItemPanel(from);
        if (panel.selected && !Tray.isInTray()) return; // don't do anything if we're chatting and not in tray
        panel.incrementUnread();
    }

    @Override
    public void onLoggedIn(User as) {
        meCard.update();
    }

    @Override
    public void onUsernameChanged(String newUsername) {
        meCard.update();
    }

    @Override
    public void onStatusChanged(User.Status newStatus) {
        meCard.update();
    }

    // ===== Observer callbacks =====
    @Override
    public void onDiscoverContact(Contact contact) {
        SwingUtilities.invokeLater(() -> addOrUpdateContactPanel(contact));
    }

    @Override
    public void onContactStatusUpdate(Contact contact) {
        SwingUtilities.invokeLater(() -> {
            ContactItemPanel panel = contactPanels.get(contact.getAddress());
            if (panel != null) {
                panel.update();
            }
        });
    }

    @Override
    public void onContactUsernameChange(Contact contact, String oldUsername, String newUsername) {
        SwingUtilities.invokeLater(() -> {
            ContactItemPanel panel = contactPanels.get(contact.getAddress());
            if (panel != null) {
                panel.setUsername(newUsername);
            }
        });
    }

    @Override
    public void onContactDisconnect(Contact contact) {
        SwingUtilities.invokeLater(() -> {
            ContactItemPanel panel = contactPanels.get(contact.getAddress());
            if (panel != null) {
                panel.update();
            }
        });
    }
}
