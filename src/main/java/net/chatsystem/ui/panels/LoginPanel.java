package net.chatsystem.ui.panels;

import net.chatsystem.models.User;
import net.chatsystem.network.discovery.DiscoveryServer;
import net.chatsystem.observer.IObserver;
import net.chatsystem.ui.MainFrame;
import net.chatsystem.ui.colors.Palette;
import net.chatsystem.ui.fonts.FontRepository;
import net.chatsystem.ui.util.PanelUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends JPanel implements IObserver {

    private final JTextField usernameField;
    private final JButton loginButton;
    private final JLabel errorLabel;

    private final Timer loginTimer;

    public LoginPanel() {
        setLayout(new GridBagLayout());
        setBackground(Palette.DEEP_BACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // ===== BIG TITLE =====
        JLabel bigTitle = new JLabel("ABYSS CHAT");
        bigTitle.setFont(FontRepository.TitleFont.deriveFont(Font.BOLD, 96f));
        bigTitle.setForeground(Palette.MAIN);
        bigTitle.setBorder(new EmptyBorder(0, 0, 30, 0));

        gbc.gridy = 0;
        add(bigTitle, gbc);

        // ===== CARD PANEL =====
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Palette.CARD_BACK);
        card.setBorder(new EmptyBorder(30, 30, 30, 30));
        card.setPreferredSize(new Dimension(360, 220));

        JLabel usernameLabel = PanelUtilities.createLabel("USERNAME");
        usernameField = PanelUtilities.createField("Username");

        loginButton = PanelUtilities.createButton("SIGN IN");

        loginTimer = new Timer(3000, e -> MainFrame.getInstance().show(MainFrame.Page.CHAT));
        loginTimer.setRepeats(false);

        errorLabel = PanelUtilities.createLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setVisible(false);

        DiscoveryServer.getInstance().addObserver(this);
        loginButton.addActionListener(action -> {
            String username = usernameField.getText().trim();
            if (username.length() < 2) {
                errorLabel.setVisible(true);
                errorLabel.setText("Username must be at least 2 characters long");
                return;
            }
            User.getInstance().setUsername(username);
            DiscoveryServer.getInstance().attemptLogin();
            usernameField.setEnabled(false);
            loginButton.setEnabled(false);
            loginButton.setText("SIGNING IN...");
            loginTimer.restart();
            errorLabel.setVisible(false);
        });

        card.add(Box.createVerticalGlue());
        card.add(usernameLabel);
        card.add(usernameField);
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(loginButton);
        card.add(Box.createVerticalGlue());

        gbc.gridy = 1;
        add(card, gbc);
    }

    @Override
    public void onUsernameTaken() {
        usernameField.setEnabled(true);
        loginButton.setEnabled(true);
        loginButton.setText("SIGN IN");
        loginTimer.stop();
        errorLabel.setVisible(true);
        errorLabel.setText("Username already taken");
    }

}
