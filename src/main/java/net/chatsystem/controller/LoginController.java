package net.chatsystem.controller;

import net.chatsystem.models.Contact;
import net.chatsystem.models.ContactList;
import net.chatsystem.models.User;
import net.chatsystem.network.chat.ChatSession;
import net.chatsystem.network.discovery.DiscoveryServer;
import net.chatsystem.network.exceptions.ChatException;
import net.chatsystem.network.exceptions.RecipientOfflineException;
import net.chatsystem.network.exceptions.UnknownRecipientException;
import net.chatsystem.observer.IObserver;

import java.util.List;
import java.util.Scanner;

public class LoginController extends Thread implements IObserver {

    private static final LoginController instance = new LoginController();

    public static LoginController getInstance() {
        return instance;
    }

    @Override
    public void onDiscoverContact(Contact contact) {
        CommandLine.success("+ " + contact.getPrintableName());
    }

    @Override
    public void onContactDisconnect(Contact contact) {
        CommandLine.error("- " + contact.getPrintableName());
    }

    @Override
    public void onContactUsernameChange(Contact contact, String old, String fresh ) {
        CommandLine.info("Contact {} is now {}", old, fresh);
    }

    public enum ControllerState {
        NOT_LOGGEDIN,
        WAITING_FOR_LOGIN_CONFIRMATION,
        WAITING_FOR_COMMAND,
        CHANGING_USERNAME,
        WAITING_FOR_USERNAME_CHANGE_CONFIRMATION,
        CHAT_CHOOSE_CONTACT,
        IN_CHAT,
        END
    }

    public ControllerState state = ControllerState.NOT_LOGGEDIN;
    User user = User.getInstance();

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);

        boolean running = true;
        CommandLine.clearAll();
        CommandLine.info("""
                                                                             
▄████▄ ▄▄▄▄  ▄▄ ▄▄  ▄▄▄▄  ▄▄▄▄   ▄█████ ▄▄ ▄▄  ▄▄▄ ▄▄▄▄▄▄ 
██▄▄██ ██▄██ ▀███▀ ███▄▄ ███▄▄   ██     ██▄██ ██▀██  ██   
██  ██ ██▄█▀   █   ▄▄██▀ ▄▄██▀   ▀█████ ██ ██ ██▀██  ██ 

                """);
        while (running) {
            switch (state) {
                case NOT_LOGGEDIN -> handleNotLoggedIn(sc);
                case WAITING_FOR_COMMAND -> handleWaitingForCommand(sc);
                case CHANGING_USERNAME -> handleChangingUsername(sc);
                case CHAT_CHOOSE_CONTACT -> handleChooseContact(sc);
                case IN_CHAT -> handleChat(sc);
                case END -> running = false;
            }
        }
        sc.close();
        CommandLine.error("Disconnected. Goodbye!");
    }

    private void handleNotLoggedIn(Scanner sc) {

        String usernameInput = CommandLine.prompt("To log in, please input a username", sc);

        if (usernameInput.isEmpty()) {
            CommandLine.clearLine();
            CommandLine.error("Username cannot be empty. Try again.");
            return;
        }

        user.username = usernameInput;
        DiscoveryServer.getInstance().attemptLogin();
        CommandLine.clearLine();

        state = ControllerState.WAITING_FOR_LOGIN_CONFIRMATION;
        CommandLine.info("Logging in...");

    }

    private void handleWaitingForCommand(Scanner sc) {

        String command = CommandLine.prompt("", sc);
        switch (command) {
            case "/help" -> {
                CommandLine.success("Available commands");
                CommandLine.info("{} > you just tried it you know what it does :p", "/help");
                CommandLine.info("{} > check your friend list", "/contacts");
                CommandLine.info("{} > sometimes it's nice to know who you truly are", "/me");
                CommandLine.info("{} > but other times it's good to make a change", "/changeusername");
                CommandLine.info("{} > would hate to see you leave!", "/disconnect");
            }
            case "/chat" -> {
                state = ControllerState.CHAT_CHOOSE_CONTACT;
            }
            case "/contacts" -> {
                if (ContactList.getInstance().getContacts().isEmpty()) {
                    CommandLine.error("You're all alone :( but it's okay!");
                }
                for (Contact contact : ContactList.getInstance().getContacts()) {
                    CommandLine.info(contact.getPrintableName());
                }
            }
            case "/me" -> {
                CommandLine.info(user.getPrintableName());
            }
            case "/changeusername" -> {
                state = ControllerState.CHANGING_USERNAME;
            }
            case "/disconnect" -> {
                DiscoveryServer.getInstance().disconnect();
                state = ControllerState.END;
                CommandLine.info("Disconnecting...");
            }
            case "" -> {} // ignore empty input
            default -> CommandLine.error("Unknown command. Try {}", "/help");
        }
    }

    private void handleChangingUsername(Scanner sc) {
        
        String input = CommandLine.prompt("Please input your new username, to cancel, write /cancel", sc);
        CommandLine.clearLine();

        if (input.startsWith("/cancel")) {
            CommandLine.error("Username change cancelled");
            state = ControllerState.WAITING_FOR_COMMAND;
            return;
        }

        if (input.isEmpty()) {
            CommandLine.error("Username cannot be empty. Try again or {}", "/cancel");
            return;
        }

        if (input.equals(user.getUsername())) {
            CommandLine.error("You're already called {}, buddy", input);
            return;
        }

        DiscoveryServer.getInstance().changeUsername(input);
        CommandLine.info("Changing username...");

        state = ControllerState.WAITING_FOR_USERNAME_CHANGE_CONFIRMATION;
    }

    // initiate chat

    private ChatSession currentChat;

    void handleChooseContact(Scanner sc) {
        List<Contact> list = ContactList.getInstance().getContacts();
        CommandLine.success("Choose your recipient (/cancel to cancel)");
        int i = 1;
        for(Contact c : list) {
            CommandLine.info("{}. {}", i++, c.getPrintableName());
        }
        String input = CommandLine.prompt("", sc);
        if (input.equals("/cancel")) {
            CommandLine.error("Chat cancelled");
            state = ControllerState.WAITING_FOR_COMMAND;
            return;
        }
        int target;
        try {
            target = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            CommandLine.error("Invalid number, try again.");
            return;
        }
        if (target < 1 || target > list.size()) {
            CommandLine.error("Invalid number, try again.");
            return;
        }
        Contact recipient = list.get(target-1);
        try {
            this.currentChat = new ChatSession(recipient);
            this.currentChat.attemptOpen();
        } catch (RecipientOfflineException e) {
            CommandLine.error("Recipient is offline");
            state = ControllerState.WAITING_FOR_COMMAND;
            return;
        }
        state = ControllerState.IN_CHAT;
        CommandLine.success("You are now chatting with {}, write /close to close chat", recipient.getPrintableName());
    }

    void handleChat(Scanner sc) {
        String input = CommandLine.prompt("", sc);
        try {
            if (input.equals("/close")) {
                this.currentChat.close();
                state = ControllerState.WAITING_FOR_COMMAND;
            }
            this.currentChat.send(input);
            CommandLine.clearLine();
            CommandLine.info("{}: {}", User.getInstance().getUsername(), input);
        } catch (ChatException chatException) {
            CommandLine.error("Unable to chat {}", chatException.getMessage());
        } catch (UnknownRecipientException e) {
            CommandLine.error("Unknown recipient (don't worry it's not your fault)");
        }
    }

    @Override
    public void onLoggedIn(User as) {
        CommandLine.success("You are now logged in as {}!", user.getUsername());
        CommandLine.info("Start with {} for a list of available commands", "/help");
        state = ControllerState.WAITING_FOR_COMMAND;
    }

    @Override
    public void onUsernameTaken() {
        switch(state) {
            case WAITING_FOR_LOGIN_CONFIRMATION -> {
                CommandLine.error("Username already taken. Try again.");
                state = ControllerState.NOT_LOGGEDIN;
            }
            case WAITING_FOR_USERNAME_CHANGE_CONFIRMATION -> {
                CommandLine.error("Username already taken. Try again or {}", "/cancel");
                state = ControllerState.CHANGING_USERNAME;
            }
        }
    }

    @Override
    public void onUsernameChanged(String newUsername) {
        CommandLine.success("Username successfully changed to {}", newUsername);
        state = ControllerState.WAITING_FOR_COMMAND;
    }

    // Chatting
    @Override
    public void onChatMessage(Contact from, String chat) {
        CommandLine.info("{}: {}", from.getUsername(), chat);
    }

    @Override
    public void onChatInitiate(Contact from) {
        CommandLine.success("{} started a chat with you! To chat back, use {}", from.getUsername(), "{}");
    }

    @Override
    public void onChatClose(Contact from) {
        if (this.currentChat != null
                && from.getAddress().equals(this.user.getAddress())
                && state == ControllerState.IN_CHAT) {
            state = ControllerState.WAITING_FOR_COMMAND;
        }
        CommandLine.info("{} ended the chat with you!", from.getUsername());
    }


}
