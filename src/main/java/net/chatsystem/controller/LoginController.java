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
    }

    @Override
    public void run() {
        User user = User.getInstance();
        while (true) {
            try {
                Scanner sc = new Scanner(System.in);
                if (!loggedIn.get()) System.out.print("To log in, please input a username: ");
                String nextCommand = sc.nextLine();
                if (!loggedIn.get()) {
                    usernameTaken.set(false);
                    user.username = nextCommand;
                    DiscoveryServer.getInstance().attemptLogin();
                    System.out.println("Logging in...");
                    Thread.sleep(3000); // 3s
                    if (usernameTaken.get()) {
                        System.out.println("Username already taken, try again.");
                        continue;
                    }
                    System.out.println("You are now logged in! Try /contacts to see contacts, or /me to see your username");
                    loggedIn.set(true);
                    continue;
                }
                switch (nextCommand) {
                    case "/contacts" -> {
                        int i = 1;
                        for (Contact contact : ContactList.getInstance().getContacts()) {
                            System.out.printf("%d. %s (%s)%n", i++, contact.getUsername(), contact.getUUID());
                        }
                    }
                    case "/me" -> System.out.printf("You are %s (%s).%n", user.getUsername(), user.getUUID());
                }
            } catch (InterruptedException ignored) {

            }
        }
    }
}
