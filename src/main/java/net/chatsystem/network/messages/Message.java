package net.chatsystem.network.messages;

import net.chatsystem.models.Contact;
import net.chatsystem.models.ContactList;
import net.chatsystem.models.User;
import net.chatsystem.network.exceptions.InvalidMessageException;
import net.chatsystem.network.exceptions.UnknownSenderException;

import java.net.InetAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Message {

    public enum Type {
        NONE,

        // udp
        DISCOVER_ME,
        ACKNOWLEDGE_DISCOVER,
        USERNAME_ALREADY_TAKEN,
        CHANGE_USERNAME_REQUEST,
        STATUS_CHANGE,
        DISCONNECT,

        // tcp
        CHAT_IDENTIFY,
        CHAT_MESSAGE,

    }

    private final Type type;
    private final String content;
    private final InetAddress address;

    public Message(Type type, String content, InetAddress address) {
        this.type = type;
        this.content = content;
        this.address = address;
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public InetAddress getAddress() {
        return address;
    }

    public static Message parse(byte[] buffer, int length, InetAddress address) throws InvalidMessageException {
        try {

            if (length < Integer.BYTES) throw new InvalidMessageException("Length of buffer needs to be at least " + Integer.BYTES); 

            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, length);

            // Read message type (4 bytes)
            int typeOrdinal = byteBuffer.getInt();
            Message.Type type = Message.Type.values()[typeOrdinal];

            // Remaining bytes = UTF-16LE content
            int remaining = byteBuffer.remaining();
            byte[] contentBytes = new byte[remaining];
            byteBuffer.get(contentBytes);
            String content = new String(contentBytes, StandardCharsets.UTF_16LE);

            return new Message(type, content, address);

        } catch (IndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e) {
            throw new InvalidMessageException("Invalid message format: " + e.getMessage());
        }
    }

    public byte[] toBuffer() {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_16LE);

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + contentBytes.length);
        buffer.putInt(type.ordinal());
        buffer.put(contentBytes);

        return buffer.array();
    }

    public boolean isFromMe() {
        return this.address.equals(User.getInstance().getAddress());
    }

    public Contact getSender() throws UnknownSenderException {
        return ContactList.getInstance().getContactByIP(getAddress()).orElseThrow(UnknownSenderException::new);
    }

}
