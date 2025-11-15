package net.chatsystem.controller;

import net.chatsystem.models.Contact;
import net.chatsystem.models.ContactList;
import net.chatsystem.models.User;
import net.chatsystem.network.discovery.DiscoveryServer;
import net.chatsystem.network.messages.Message;
import net.chatsystem.observer.IObserver;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoginController extends Thread implements IObserver {

    private static final LoginController instance = new LoginController();

    public static LoginController getInstance() {
        return instance;
    }

    public AtomicBoolean loggedIn = new AtomicBoolean(false);
    public AtomicBoolean usernameTaken = new AtomicBoolean(false);

    @Override
    public void onMessage(Message message) {}

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
        CommandLine.info("Contact {} username to {}", contact.getPrintableName(), fresh);
    }

    public enum ControllerState {
        NOT_LOGGEDIN,
        WAITING_FOR_COMMAND,
        CHANGING_USERNAME,
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
                    case END -> running = false;
                }
            } catch (InterruptedException e) {
                switch (state) {
                    case NOT_LOGGEDIN, CHANGING_USERNAME -> {
                            CommandLine.clearLine();
                            CommandLine.error("Username already taken, try again.");
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
        CommandLine.info("Logging in...");
        Thread.sleep(3000);
        DiscoveryServer.getInstance().setConnected();

        if (usernameTaken.get()) {
            CommandLine.clearLine();
            CommandLine.error("Username already taken. Try again.");
            // stay not logged-in
        } else {
            CommandLine.clearLine();
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
}
