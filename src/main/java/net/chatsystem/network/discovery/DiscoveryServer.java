package net.chatsystem.network.discovery;

import net.chatsystem.models.Contact;
import net.chatsystem.models.ContactList;
import net.chatsystem.models.User;
import net.chatsystem.models.exceptions.UsernameAlreadyTakenException;
import net.chatsystem.network.exceptions.InvalidMessageException;
import net.chatsystem.network.messages.Message;
import net.chatsystem.network.messages.MessageBuilder;
import net.chatsystem.observer.IObserver;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Objects;

public class DiscoveryServer extends Thread {

    private static final int MAX_BUFFER_LENGTH = 256;
    private static final InetAddress BROADCAST_ADDRESS;
    private static final InetAddress LOCALHOST;

    static {
        try {
            BROADCAST_ADDRESS = InetAddress.getByName("255.255.255.255");
            LOCALHOST = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static final int PORT = 2050;

    private final DatagramSocket socket; // Socket to listen on for discoveries
    private final ArrayList<IObserver> observers = new ArrayList<>(); // observer

    public DiscoveryServer() throws SocketException {
        this.socket = new DatagramSocket(PORT);
        socket.setBroadcast(true);
    }

    public void addObserver(IObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[MAX_BUFFER_LENGTH];
        try {
            while(true) {
                DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(inPacket);
                Message msg = Message.parse(inPacket.getData(), inPacket.getLength(), inPacket.getAddress());
                handleMessage(msg);
                if (Objects.equals(msg.getContent(), "END")) break;
            }
        } catch (IOException ex) {
            System.out.println("IO Exception while reading from UDP socket");
        } catch (InvalidMessageException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void handleMessage(Message message) {
        if (message.isFromMe()) return; // ignore self messages in broadcast
        Message.Type type = message.getType();
        for(IObserver o : observers) {
            o.onMessage(message);
        }
        switch(type) {
            case DISCOVER_ME, ACKNOWLEDGE_DISCOVER -> {
                String username = message.getContent();
                try {
                    Contact newContact = ContactList.getInstance().registerContact(username, message.getSenderUUID(), message.getAddress());
                    for(IObserver o : observers) {
                        o.onDiscoverContact(newContact);
                    }
                    if (type == Message.Type.DISCOVER_ME) {
                        Message discoverMeToo = new MessageBuilder()
                                .setType(Message.Type.ACKNOWLEDGE_DISCOVER)
                                .setContent(User.getInstance().getUsername())
                                .setAddress(message.getAddress())
                                .build();
                        sendMessage(discoverMeToo);
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
                for(IObserver o : observers) {
                    o.onNotifyUsernameTaken();
                }
            }
        }
    }

    public void sendMessage(Message message) {
        byte[] buffer = message.toBuffer();
        DatagramPacket p = new DatagramPacket(buffer, 0, buffer.length, message.getAddress(), PORT);
        try {
            socket.send(p);
        } catch(IOException ex) {
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

}
