package net.chatsystem.network.discovery;

import net.chatsystem.models.ContactList;
import net.chatsystem.models.User;
import net.chatsystem.models.exceptions.UsernameAlreadyTakenException;
import net.chatsystem.network.exceptions.InvalidMessageException;
import net.chatsystem.network.messages.Message;

import java.io.IOException;
import java.net.*;
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

    private final DatagramSocket socket;

    public DiscoveryServer() throws SocketException {
        this.socket = new DatagramSocket(PORT);
        socket.setBroadcast(true);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[MAX_BUFFER_LENGTH];
        try {
            while(true) {
                DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(inPacket);
                Message msg = Message.parse(inPacket.getData(), inPacket.getAddress());
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
        //if (message.getAddress().is()) return; // ignore self messages in broadcast
        System.out.println("Received: " + message.getType() + " from " + message.getAddress());
        switch(message.getType()) {
            case DISCOVER_ME -> {
                String username = message.getContent();
                try {
                    ContactList.getInstance().registerContact(username, message.getAddress());
                } catch (UsernameAlreadyTakenException ex) {
                    sendMessage(new Message(Message.Type.USERNAME_ALREADY_TAKEN, "", message.getAddress()));
                }
            }
            case USERNAME_ALREADY_TAKEN -> {
                System.out.println("Username already taken");
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
        Message message = new Message(Message.Type.DISCOVER_ME, username, BROADCAST_ADDRESS);
        sendMessage(message);

    }

}
