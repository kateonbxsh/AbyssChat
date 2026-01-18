package net.chatsystem.network.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import net.chatsystem.models.Contact;
import net.chatsystem.models.ContactList;
import net.chatsystem.models.User;
import net.chatsystem.models.exceptions.UsernameAlreadyTakenException;
import net.chatsystem.network.exceptions.InvalidMessageException;
import net.chatsystem.network.exceptions.UnknownSenderException;
import net.chatsystem.network.messages.Message;
import net.chatsystem.network.messages.MessageBuilder;
import net.chatsystem.observer.IObserver;

public class DiscoveryServer extends Thread {

    private static DiscoveryServer instance;

    public static DiscoveryServer getInstance() {
        if (instance == null) instance = new DiscoveryServer();
        return instance;
    }

    private static final int MAX_BUFFER_LENGTH = 256;
    public static final InetAddress BROADCAST_ADDRESS;

    static {
        try {
            BROADCAST_ADDRESS = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static int SEND_PORT = 2050;
    public static int RECEIVE_PORT = 2050;

    private DatagramSocket socket; // Socket to listen on for discoveries
    private final ArrayList<IObserver> observers = new ArrayList<>(); // observer

    public DiscoveryServer() {
        setDaemon(true);
    }

    public void bind() throws SocketException {
        this.socket = new DatagramSocket(RECEIVE_PORT);
        socket.setBroadcast(true);
    }

    public void addObserver(IObserver observer) {
        this.observers.add(observer);
    }

    private boolean connected = false;
    private boolean running = true;

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void run() {
        
        byte[] buffer = new byte[MAX_BUFFER_LENGTH];
        try {
            while (running) {
                DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(inPacket);
                Message msg = Message.parse(inPacket.getData(), inPacket.getLength(), inPacket.getAddress());
                handleMessage(msg);
            }
        } catch (IOException ignored) {
        } catch (InvalidMessageException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void handleMessage(Message message) {
        if (message.isFromMe()) return; // ignore self messages in broadcast
        Message.Type type = message.getType();
        for (IObserver o : observers) {
            o.onMessage(message);
        }
        // message types handled without needing connection
        switch (type) {
            case ACKNOWLEDGE_DISCOVER -> {
                String username = message.getContent();
                try {
                    Contact newContact = ContactList.getInstance().registerContact(username, message.getAddress());
                    for (IObserver o : observers) {
                        o.onDiscoverContact(newContact);
                    }
                } catch (UsernameAlreadyTakenException ex) {
                    Message response = new MessageBuilder()
                            .setType(Message.Type.USERNAME_ALREADY_TAKEN)
                            .setAddress(message.getAddress())
                            .build();
                    sendMessage(response);
                }
            }
            case USERNAME_ALREADY_TAKEN -> {
                for (IObserver o : observers) {
                    o.onUsernameTaken();
                }
                loginTimer.cancel();
            }
        }

        if (!connected) return;
        // message types needing connection
        switch (type) {
            case DISCOVER_ME -> {
                String username = message.getContent();
                try {
                    Contact newContact = ContactList.getInstance().registerContact(username, message.getAddress());
                    newContact.setStatus(User.Status.ONLINE); // mark as ONLINE
                    for (IObserver o : observers) {
                        o.onDiscoverContact(newContact);
                    }
                    Message discoverMeToo = new MessageBuilder()
                            .setType(Message.Type.ACKNOWLEDGE_DISCOVER)
                            .setContent(User.getInstance().getUsername())
                            .setAddress(message.getAddress())
                            .build();
                    sendMessage(discoverMeToo);
                    // send my current status as well
                    changeStatus(User.getInstance().getStatus());
                } catch (UsernameAlreadyTakenException ex) {
                    Message response = new MessageBuilder()
                            .setType(Message.Type.USERNAME_ALREADY_TAKEN)
                            .setAddress(message.getAddress())
                            .build();
                    sendMessage(response);
                }
            }
            case USERNAME_ALREADY_TAKEN -> {
                for (IObserver o : observers) {
                    o.onUsernameTaken();
                }
            }
            case CHANGE_USERNAME_REQUEST -> {
                try {
                    String oldUsername = message.getSender().getUsername();
                    // this could throw UsernameAlreadyTakenException
                    ContactList.getInstance().changeContactUsername(message.getAddress(), message.getContent());
                    for (IObserver o : observers) {
                        o.onContactUsernameChange(message.getSender(), oldUsername, message.getContent());
                    }
                } catch (UsernameAlreadyTakenException ignored) {
                    Message response = new MessageBuilder()
                            .setType(Message.Type.USERNAME_ALREADY_TAKEN)
                            .setAddress(message.getAddress())
                            .build();
                    sendMessage(response);
                } catch (UnknownSenderException ignored) {}
            }
            case STATUS_CHANGE -> {
                User.Status status;
                try {
                    status = User.Status.valueOf(message.getContent());
                    Contact contact = message.getSender();
                    contact.setStatus(status);
                    for (IObserver o : observers) {
                        o.onContactStatusUpdate(contact);
                    }
                } catch (IllegalArgumentException | UnknownSenderException ignored) {}
            }
            case DISCONNECT -> {
                Optional<Contact> c = ContactList.getInstance().getContactByIP(message.getAddress());
                if (c.isEmpty()) break;
                c.get().setStatus(User.Status.OFFLINE);
                for (IObserver o: observers) {
                    o.onContactDisconnect(c.get());
                }
            }
        }
    }

    public synchronized void sendMessage(Message message) {
        byte[] buffer = message.toBuffer();
        DatagramPacket p = new DatagramPacket(buffer, 0, buffer.length, message.getAddress(), SEND_PORT);
        try {
            socket.send(p);
        } catch (IOException ex) {
            System.out.println("Error sending message to " + message.getAddress());
        }
    }

    Timer loginTimer = new Timer();
    private static final long USERNAME_CONFIRMATION_DELAY = 1000;
    private class LoginTimerTask extends TimerTask {
        @Override
        public void run() {
            connected = true;
            for(IObserver o : observers) {
                o.onLoggedIn(User.getInstance());
            }
        }
    }
    private class UsernameChangeTask extends TimerTask {
        @Override
        public void run() {
            User.getInstance().setUsername(newUsername);
            for(IObserver o : observers) {
                o.onUsernameChanged(newUsername);
            }
        }
    }

    public synchronized void attemptLogin() {

        String username = User.getInstance().getUsername();
        Message login = new MessageBuilder()
                .setType(Message.Type.DISCOVER_ME)
                .setContent(username)
                .setAddress(BROADCAST_ADDRESS)
                .build();
        sendMessage(login);
        loginTimer = new Timer();
        loginTimer.schedule(new LoginTimerTask(), USERNAME_CONFIRMATION_DELAY);

        // while trying to log in, the user should hold the username hostage,
        // in case someone else tries to log in at the same time with that same username
        connected = true;

    }

    public synchronized void disconnect() {
        if (!running) return; //if already disconnected, no need to disconnect twice (ShutdownHook re-runs it)
        Message disconnect = new MessageBuilder()
                .setType(Message.Type.DISCONNECT)
                .setAddress(BROADCAST_ADDRESS)
                .build();
        sendMessage(disconnect);
        running = false;
        connected = false;
        socket.close();
    }

    private String newUsername;
    public synchronized void changeUsername(String username) {
        newUsername = username;
        Message login = new MessageBuilder()
                .setType(Message.Type.CHANGE_USERNAME_REQUEST)
                .setContent(username)
                .setAddress(BROADCAST_ADDRESS)
                .build();
        sendMessage(login);
        loginTimer = new Timer();
        loginTimer.schedule(new UsernameChangeTask(), USERNAME_CONFIRMATION_DELAY);
    }

    public synchronized void changeStatus(User.Status newStatus) {
        User.getInstance().setStatus(newStatus);
        Message msg = new MessageBuilder()
                .setType(Message.Type.STATUS_CHANGE)
                .setContent(newStatus.toString())
                .setAddress(BROADCAST_ADDRESS)
                .build();
        sendMessage(msg);
        for (IObserver o : observers) {
            o.onStatusChanged(newStatus);
        }
    }

}
