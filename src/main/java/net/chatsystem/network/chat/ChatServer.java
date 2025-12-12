package net.chatsystem.network.chat;

import net.chatsystem.controller.CommandLine;
import net.chatsystem.models.Contact;
import net.chatsystem.models.ContactList;
import net.chatsystem.network.exceptions.InvalidMessageException;
import net.chatsystem.network.exceptions.UnableToStartChatException;
import net.chatsystem.network.exceptions.UnknownRecipientException;
import net.chatsystem.network.exceptions.UnknownSenderException;
import net.chatsystem.network.messages.Message;
import net.chatsystem.network.messages.MessageBuilder;
import net.chatsystem.observer.IObserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer extends Thread {

    private static ChatServer instance;
    public static ChatServer getInstance() {
        if (instance == null) instance = new ChatServer();
        return instance;
    }


    private ServerSocket serverSocket;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final List<IObserver> observers = new ArrayList<>();
    private final Map<UUID, Socket> socketMap = new HashMap<>();
    public int LISTEN_PORT = 2500;
    private volatile boolean running = true;

    public ChatServer() {
        setDaemon(true);
    }

    public void addObserver(IObserver o) {
        observers.add(o);
    }

    public void bind() throws IOException {
        this.serverSocket = new ServerSocket(LISTEN_PORT);
    }

    @Override
    public void run() {
        try {
            while (running) {
                Socket s = serverSocket.accept();
                pool.submit(new ClientHandler(s));
            }
        } catch (IOException e) {
            if (running) e.printStackTrace();
        }
    }

    public void stopServer() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
        pool.shutdownNow();
    }

    public void initiateChat(Contact contact) throws UnableToStartChatException, UnknownRecipientException {
        // look for contact in socket map, if not found, initiate connection with contact
        if (!socketMap.containsKey(contact.getUUID())) {
            // initiate connection
            Socket newSocket = new Socket();
            try {
                newSocket.connect(new InetSocketAddress(contact.getRemoteAddress(), LISTEN_PORT));
            } catch (IOException ioException) {
                throw new UnableToStartChatException(ioException);
            }
            socketMap.put(contact.getUUID(), newSocket);
            pool.submit(new ClientHandler(newSocket, contact));
            // identify
            Message identify = new MessageBuilder()
                    .setRecipient(contact)
                    .setType(Message.Type.CHAT_IDENTIFY)
                    .build();
            try {
                sendMessage(contact.getUUID(), identify);
            } catch(IOException io) {
                throw new UnableToStartChatException(io);
            }
        } // otherwise, we already have a client handler for the socket
    }

    public void sendMessage(UUID recipient, Message message) throws UnknownRecipientException, IOException {
        if (!socketMap.containsKey(recipient)) throw new UnknownRecipientException(recipient);
        Socket socket = socketMap.get(recipient);
        byte[] buffer = message.toBuffer();
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeInt(buffer.length);
        out.write(buffer);
        out.flush();
    }

    public void stopChat(Contact with) throws UnknownRecipientException, IOException {
        if (!socketMap.containsKey(with.getUUID())) throw new UnknownRecipientException(with.getUUID());
        Socket socket = socketMap.get(with.getUUID());
        socket.close();
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        ClientHandler(Socket socket, Contact contact) {
            this.socket = socket;
            this.identified = true;
            this.contact = contact;
        }

        boolean identified = false; // whether the user has sent a CHAT_IDENTIFY packet
        Contact contact = null; // associated contact with this socket

        @Override
        public void run() {
            try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
                while (!socket.isClosed() && running) {
                    int length;
                    length = in.readInt();
                    if (length <= 0) break;
                    byte[] buf = in.readNBytes(length);
                    Message msg = Message.parse(buf, length, socket.getInetAddress());
                    for (IObserver o : observers) o.onMessage(msg);
                    handleMessage(msg);
                }
            } catch (IOException | InvalidMessageException | UnknownSenderException ignored) {
            } finally {
                try {
                    if (identified) {
                        socketMap.remove(this.contact.getUUID());
                    }
                    if (!socket.isClosed()) {
                        for(IObserver o : observers) {
                            o.onChatClose(this.contact);
                        }
                        socket.close();
                    }
                } catch (IOException ignored) {}
            }
        }

        public void handleMessage(Message msg) throws UnknownSenderException {
            switch(msg.getType()) {
                case CHAT_IDENTIFY -> {
                    this.identified = true;
                    this.contact = msg.getSender();
                    socketMap.put(msg.getSenderUUID(), this.socket);
                    for(IObserver o : observers) {
                        o.onChatInitiate(this.contact);
                    }
                }
                case CHAT_MESSAGE -> {
                    for(IObserver o : observers) {
                        o.onChatMessage(this.contact, msg.getContent());
                    }
                }
                default ->  {}
            }
        }
    }
}