package net.chatsystem.network.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;

import net.chatsystem.models.Contact;
import net.chatsystem.models.ContactList;
import net.chatsystem.models.User;
import net.chatsystem.models.exceptions.UsernameAlreadyTakenException;
import net.chatsystem.network.exceptions.InvalidMessageException;
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
    public void setConnected() {
        connected = true;
    }
    public void setDisconnected() {
        connected = false;
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
                    Contact newContact = ContactList.getInstance().registerContact(username, message.getSenderUUID(), message.getAddress());
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
                    o.onNotifyUsernameTaken();
                }
            }
        }

        if (!connected) return;
        // message types needing connection
        switch (type) {
            case DISCOVER_ME -> {
                String username = message.getContent();
                try {
                    Contact newContact = ContactList.getInstance().registerContact(username, message.getSenderUUID(), message.getAddress());
                    for (IObserver o : observers) {
                        o.onDiscoverContact(newContact);
                    }
                    Message discoverMeToo = new MessageBuilder()
                            .setType(Message.Type.ACKNOWLEDGE_DISCOVER)
                            .setContent(User.getInstance().getUsername())
                            .setAddress(message.getAddress())
                            .build();
                    sendMessage(discoverMeToo);
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
                    o.onNotifyUsernameTaken();
                }
            }
            case CHANGE_USERNAME_REQUEST -> {
                try {
                    String oldUsername = message.getSender().getUsername();
                    // this could throw UsernameAlreadyTakenException
                    ContactList.getInstance().changeContactUsername(message.getSenderUUID(), message.getContent());
                    for (IObserver o : observers) {
                        o.onContactUsernameChange(message.getSender(), oldUsername, message.getContent());
                    }
                } catch (UsernameAlreadyTakenException ignored) {
                    Message response = new MessageBuilder()
                            .setType(Message.Type.USERNAME_ALREADY_TAKEN)
                            .setAddress(message.getAddress())
                            .build();
                    sendMessage(response);
                }
            }
            case DISCONNECT -> {
                Optional<Contact> c = ContactList.getInstance().getContactByUUID(message.getSenderUUID());
                if (c.isEmpty()) break;
                for (IObserver o: observers) {
                    o.onContactDisconnect(c.get());
                }
                ContactList.getInstance().unregisterContact(c.get());
            }
        }
    }

    public void sendMessage(Message message) {
        byte[] buffer = message.toBuffer();
        DatagramPacket p = new DatagramPacket(buffer, 0, buffer.length, message.getAddress(), SEND_PORT);
        try {
            socket.send(p);
        } catch (IOException ex) {
            System.out.println("Error sending message to " + message.getAddress());
        }
    }

    public void attemptLogin() {

        String username = User.getInstance().getUsername();
        Message login = new MessageBuilder()
                .setType(Message.Type.DISCOVER_ME)
                .setContent(username)
                .setAddress(BROADCAST_ADDRESS)
                .build();
        sendMessage(login);
    }

    public void disconnect() {
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

    public void changeUsername(String username) {
        Message login = new MessageBuilder()
                .setType(Message.Type.CHANGE_USERNAME_REQUEST)
                .setContent(username)
                .setAddress(BROADCAST_ADDRESS)
                .build();
        sendMessage(login);
    }

}
