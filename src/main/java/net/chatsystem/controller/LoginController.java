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
        System.out.println("New contact: " + contact.username);
    }

    @Override
    public void onNotifyUsernameTaken() {
        usernameTaken.set(true);
        this.interrupt();
    }

    @Override
    public void onContactDisconnect(Contact contact) {
        System.out.println("Contact disconnected: " + contact.username);
    }

    @Override
    public void onContactUsernameChange(Contact contact, String old, String fresh ) { //can't use new
        System.out.println("Contact changed username from " + old + " to " + fresh);
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
                    case NOT_LOGGEDIN, CHANGING_USERNAME ->
                            System.out.println("Username already taken, try again.");
                }
            }

        }
        sc.close();
        System.out.println("Disconnected. Goodbye!");
    }

    private void handleNotLoggedIn(Scanner sc) throws InterruptedException {
        System.out.print("To log in, please input a username: ");
        String usernameInput = sc.nextLine().trim();

        if (usernameInput.isEmpty()) {
            System.out.println("Username cannot be empty. Try again.");
            return;
        }

        usernameTaken.set(false);
        user.username = usernameInput;
        DiscoveryServer.getInstance().attemptLogin();
        System.out.println("Logging in...");
        Thread.sleep(3000);
        DiscoveryServer.getInstance().setConnected();

        if (usernameTaken.get()) {
            System.out.println("Username already taken, try again.");
            // Stay not logged-in
        } else {
            System.out.println("You are now logged in! Try /contacts to see contacts, or /me to see your username");
            loggedIn.set(true);
            state = ControllerState.WAITING_FOR_COMMAND;
        }
    }

    private void handleWaitingForCommand(Scanner sc) throws InterruptedException {
        String command = sc.nextLine().trim();

        switch (command) {
            case "/contacts" -> {
                int i = 1;
                for (Contact contact : ContactList.getInstance().getContacts()) {
                    System.out.printf("%d. %s (%s)%n", i++, contact.getUsername(), contact.getUUID());
                }
            }
            case "/me" -> {
                System.out.printf("You are %s (%s).%n", user.getUsername(), user.getUUID());
            }
            case "/changeusername" -> {
                state = ControllerState.CHANGING_USERNAME;
            }
            case "/disconnect" -> {
                DiscoveryServer.getInstance().disconnect();
                loggedIn.set(false);
                state = ControllerState.END;
                System.out.println("Disconnecting...");
            }
            case "" -> {} // Ignore empty input
            default -> System.out.println("Unknown command. Available: /contacts, /me, /changeusername, /disconnect");
        }
    }

    private void handleChangingUsername(Scanner sc) throws InterruptedException {
        System.out.print("Please input your new username, to cancel, write /cancel: ");
        String input = sc.nextLine().trim();

        if (input.startsWith("/cancel")) {
            System.out.println("Username change cancelled.");
            state = ControllerState.WAITING_FOR_COMMAND;
            return;
        }

        if (input.isEmpty()) {
            System.out.println("Username cannot be empty. Try again or /cancel.");
            return;
        }

        usernameTaken.set(false);
        DiscoveryServer.getInstance().changeUsername(input);
        System.out.println("Changing username...");
        Thread.sleep(3000);

        if (usernameTaken.get()) {
            System.out.println("Username already taken. Try again or /cancel.");
            // Stay in changing_username
        } else {
            user.username = input;
            System.out.println("Username successfully changed to: " + input);
            state = ControllerState.WAITING_FOR_COMMAND;
        }
    }
}
