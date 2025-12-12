package net.chatsystem.controller;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import net.chatsystem.models.Contact;
import net.chatsystem.models.ContactList;
import net.chatsystem.models.User;
import net.chatsystem.network.chat.Chat;
import net.chatsystem.network.discovery.DiscoveryServer;
import net.chatsystem.network.exceptions.ChatException;
import net.chatsystem.network.exceptions.UnableToStartChatException;
import net.chatsystem.network.exceptions.UnknownRecipientException;
import net.chatsystem.observer.IObserver;

public class LoginController extends Thread implements IObserver {

    private static final LoginController instance = new LoginController();

    public static LoginController getInstance() {
        return instance;
    }

    public AtomicBoolean loggedIn = new AtomicBoolean(false);
    public AtomicBoolean usernameTaken = new AtomicBoolean(false);

    @Override
    public void onDiscoverContact(Contact contact) {
        CommandLine.success("+ " + contact.getPrintableName());
    }

    @Override
    public void onNotifyUsernameTaken() {
        usernameTaken.set(true);
        // interrupt this thread so user doesn't have to keep waiting :p
        this.interrupt();
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
        WAITING_FOR_COMMAND,
        CHANGING_USERNAME,
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
            try {
                switch (state) {
                    case NOT_LOGGEDIN -> handleNotLoggedIn(sc);
                    case WAITING_FOR_COMMAND -> handleWaitingForCommand(sc);
                    case CHANGING_USERNAME -> handleChangingUsername(sc);
                    case CHAT_CHOOSE_CONTACT -> handleChooseContact(sc);
                    case IN_CHAT -> handleChat(sc);
                    case END -> running = false;
                }
            } catch (InterruptedException e) {
                switch (state) {
                    case CHANGING_USERNAME -> {
                        CommandLine.clearLine();
                        CommandLine.error("Username already taken, try again.");
                    }
                    case NOT_LOGGEDIN -> {
                        CommandLine.clearLine();
                        CommandLine.error("Username already taken, try again.");
                        DiscoveryServer.getInstance().setDisconnected();
                    }
                    default -> {}
                }
            }

        }
        sc.close();
        CommandLine.error("Disconnected. Goodbye!");
    }

    private void handleNotLoggedIn(Scanner sc) throws InterruptedException {

        String usernameInput = CommandLine.prompt("To log in, please input a username", sc);

        if (usernameInput.isEmpty()) {
            CommandLine.clearLine();
            CommandLine.error("Username cannot be empty. Try again.");
            return;
        }

        usernameTaken.set(false);
        user.username = usernameInput;
        DiscoveryServer.getInstance().attemptLogin();
        CommandLine.clearLine();
        DiscoveryServer.getInstance().setConnected();
        // while trying to log in, the user holds on to its username
        // this is in case while waiting for confirmation, someone tries to take that username
        // in which case they will consider that they took it first
        CommandLine.info("Logging in...");
        Thread.sleep(3000);

        if (usernameTaken.get()) {
            CommandLine.clearLine();
            CommandLine.error("Username already taken. Try again.");
            DiscoveryServer.getInstance().setDisconnected();
            // stay not logged-in
        } else {
            CommandLine.success("You are now logged in!");
            CommandLine.info("Start with {} for a list of available commands", "/help");
            loggedIn.set(true);
            state = ControllerState.WAITING_FOR_COMMAND;
        }
    }

    private void handleWaitingForCommand(Scanner sc) throws InterruptedException {

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
                loggedIn.set(false);
                state = ControllerState.END;
                CommandLine.info("Disconnecting...");
            }
            case "" -> {} // ignore empty input
            default -> CommandLine.error("Unknown command. Try {}", "/help");
        }
    }

    private void handleChangingUsername(Scanner sc) throws InterruptedException {
        
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

        usernameTaken.set(false);
        DiscoveryServer.getInstance().changeUsername(input);
        CommandLine.info("Changing username...");
        Thread.sleep(3000);

        CommandLine.clearLine();
        if (usernameTaken.get()) {
            CommandLine.error("Username already taken. Try again or {}", "/cancel");
            // Stay in changing_username
        } else {
            user.username = input;
            CommandLine.success("Username successfully changed to {}", input);
            state = ControllerState.WAITING_FOR_COMMAND;
        }
    }

    // initiate chat

    private Chat currentChat;

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
            this.currentChat = new Chat(recipient);
        } catch (UnableToStartChatException e) {
            CommandLine.error("Unable to start chat, reason: {}", e.getMessage());
            state = ControllerState.WAITING_FOR_COMMAND;
            return;
        } catch (UnknownRecipientException e) {
            CommandLine.error("Unknown recipient (don't worry it's not your fault) {}", e.who);
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
                && from.getUUID() == this.currentChat.recipient.getUUID()
                && state == ControllerState.IN_CHAT) {
            state = ControllerState.WAITING_FOR_COMMAND;
        }
        CommandLine.info("{} ended the chat with you!", from.getUsername());
    }


}
