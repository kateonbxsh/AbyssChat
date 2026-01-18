package net.chatsystem.ui.panels;

import net.chatsystem.models.User;
import net.chatsystem.network.discovery.DiscoveryServer;
import net.chatsystem.observer.IObserver;
import net.chatsystem.ui.MainFrame;
import net.chatsystem.ui.colors.Palette;
import net.chatsystem.ui.fonts.FontRepository;
import net.chatsystem.ui.util.PanelUtilities;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EditProfilePanel extends JPanel implements IObserver {

    private final JLabel usernameLabel;
    private final JLabel errorLabel;
    private final JLabel warningLabel;
    private final JButton saveButton;
    private final JButton cancelButton;
    private final JComboBox<User.Status> statusBox;
    private final JTextField usernameField;

    public EditProfilePanel() {
        setLayout(new BorderLayout());
        setBackground(Palette.DEEP_BACK);

        // === Title ===
        JLabel title = new JLabel("MY PROFILE", SwingConstants.CENTER);
        title.setFont(FontRepository.TitleFont.deriveFont(48f));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));
        add(title, BorderLayout.NORTH);

        // === Center panel for username + status ===
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setBackground(getBackground());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        usernameLabel = PanelUtilities.createLabel(User.getInstance().getUsername() + " @ " + User.getInstance().getAddress().toString());
        centerPanel.add(usernameLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;

        // Username label + field
        JLabel changeUsernameLabel = PanelUtilities.createLabel("Change username ");
        centerPanel.add(changeUsernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = PanelUtilities.createField("Change username...");
        centerPanel.add(usernameField, gbc);

        // Status label + dropdown
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel statusLabel = PanelUtilities.createLabel("Status ");
        centerPanel.add(statusLabel, gbc);

        gbc.gridx = 1;
        User.Status[] statuses = User.Status.values();
        statusBox = new JComboBox<>(statuses);
        statusBox.setFont(FontRepository.LabelFont);
        centerPanel.add(statusBox, gbc);

        Color boxBack = Palette.DEEP_BACK.darker().darker();
        statusBox.setBackground(boxBack);
        statusBox.setForeground(Palette.statusToColor(User.Status.ONLINE));

        statusBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = super.createArrowButton();
                button.setBackground(Palette.APP_BACK); // arrow button color
                return button;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {}
        });

        statusBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                User.Status status = (User.Status) value;

                label.setText(User.getStatusName(status, true));

                label.setBackground(boxBack);
                label.setForeground(Palette.statusToColor(status));

                label.setFont(FontRepository.LabelFont);

                return label;
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        errorLabel = PanelUtilities.createLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setVisible(false);
        centerPanel.add(errorLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;

        warningLabel = PanelUtilities.createLabel("");
        warningLabel.setForeground(Color.YELLOW);
        warningLabel.setVisible(false);
        centerPanel.add(warningLabel, gbc);

        statusBox.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                User.Status selected = (User.Status) itemEvent.getItem();
                statusBox.setForeground(Palette.statusToColor(selected));
                if (selected == User.Status.OFFLINE) {
                    warningLabel.setText("Careful, people will not be able to text you when setting status to invisible!");
                    warningLabel.setVisible(true);
                } else {
                    warningLabel.setText("");
                    warningLabel.setVisible(false);
                }
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 5;

        saveButton = PanelUtilities.createButton("SAVE CHANGES");
        centerPanel.add(saveButton, gbc);

        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // if username is not set, ignore changing username
                User.Status selectedStatus = (User.Status) statusBox.getSelectedItem();
                String selectedUsername = usernameField.getText().trim();

                if (!selectedUsername.isBlank() && selectedUsername.length() < 2) {
                    errorLabel.setText("Username must be at least 2 characters long");
                    return;
                }
                errorLabel.setText("");

                if (selectedStatus != User.getInstance().getStatus()) {
                    DiscoveryServer.getInstance().changeStatus(selectedStatus);
                    User.getInstance().setStatus(selectedStatus);
                    statusBox.setSelectedItem(selectedStatus);
                }
                if (!selectedUsername.isBlank()) {
                    saveButton.setText("SAVING...");
                    saveButton.setEnabled(false);
                    cancelButton.setEnabled(false);
                    statusBox.setEnabled(false);
                    awaitingSaving = true;
                    DiscoveryServer.getInstance().changeUsername(selectedUsername);
                    return;
                }
                MainFrame.getInstance().show(MainFrame.Page.CHAT);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 6;

        cancelButton = PanelUtilities.createButton("CANCEL");
        centerPanel.add(cancelButton, gbc);

        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                usernameField.setText("");
                statusBox.setSelectedItem(User.getInstance().getStatus());
                MainFrame.getInstance().show(MainFrame.Page.CHAT);
            }
        });

        add(centerPanel, BorderLayout.CENTER);

        DiscoveryServer.getInstance().addObserver(this);

    }

    private boolean awaitingSaving = false;

    @Override
    public void onLoggedIn(User as) {
        updateUsername(as.getUsername());
    }

    @Override
    public void onUsernameChanged(String newUsername) {
        updateUsername(newUsername);
        if (awaitingSaving) {
            saveButton.setText("SAVE CHANGES");
            saveButton.setEnabled(true);
            cancelButton.setEnabled(true);
            statusBox.setEnabled(true);
            usernameField.setText("");
            MainFrame.getInstance().show(MainFrame.Page.CHAT);
        }
    }

    @Override
    public void onUsernameTaken() {
        if (awaitingSaving) {
            saveButton.setEnabled(true);
            cancelButton.setEnabled(true);
            statusBox.setEnabled(true);
            saveButton.setText("SAVE CHANGES");
            errorLabel.setText("Username already taken, try again");
            errorLabel.setVisible(true);
        }
    }

    @Override
    public void onStatusChanged(User.Status newStatus) {
        statusBox.setSelectedItem(newStatus);
    }

    private void updateUsername(String newUsername) {
        usernameLabel.setText(newUsername + " @ " + User.getInstance().getAddress());
    }

}
