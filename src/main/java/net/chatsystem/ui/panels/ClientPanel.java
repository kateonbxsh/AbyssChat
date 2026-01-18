package net.chatsystem.ui.panels;

import net.chatsystem.ui.colors.Palette;
import net.chatsystem.ui.panels.chat.ChatPanel;
import net.chatsystem.ui.panels.contact.ContactsPanel;

import javax.swing.*;
import java.awt.*;

public class ClientPanel extends JPanel {

    public ClientPanel() {
        setLayout(new BorderLayout());
        setBackground(Palette.DEEP_BACK);

        ChatPanel chatPanel = new ChatPanel();
        ContactsPanel contactsPanel = new ContactsPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatPanel, contactsPanel);
        splitPane.setResizeWeight(0.8);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        splitPane.setBackground(Palette.APP_BACK);

        add(splitPane, BorderLayout.CENTER);
    }
}
