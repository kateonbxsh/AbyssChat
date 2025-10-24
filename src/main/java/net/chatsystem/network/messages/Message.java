package net.chatsystem.network.messages;

import net.chatsystem.network.exceptions.InvalidMessageException;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Message {

    public enum Type {
        DISCOVER_ME,
        USERNAME_ALREADY_TAKEN
    }

    private final Type type;
    private final String content;
    private final InetAddress address;

    public Message(Type type, String content, InetAddress address) {
        this.type = type;
        this.content = content;
        this.address = address;
    }

    public Type getType() { return type; }
    public String getContent() { return content; }
    public InetAddress getAddress() { return address; }

    public static Message parse(byte[] buffer, InetAddress address) throws InvalidMessageException {
        if (buffer.length < 4) throw new InvalidMessageException("Buffer length: " + buffer.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        int type = byteBuffer.getInt();
        String content = new String(byteBuffer.slice().array(), StandardCharsets.UTF_8);
        return new Message(Type.values()[type], content, address);
    }

    public byte[] toBuffer() {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] buffer = new byte[4 + contentBytes.length];
        return ByteBuffer.wrap(buffer).putInt(type.ordinal()).put(contentBytes).array();
    }

}
