package net.chatsystem.network.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.chatsystem.models.Contact;
import net.chatsystem.models.ContactList;
import net.chatsystem.models.User;
import net.chatsystem.network.Network;
import net.chatsystem.network.exceptions.InvalidMessageException;
import net.chatsystem.network.exceptions.UnknownSenderException;

@DisplayName("Message serialization & parsing tests")
class MessageTest {

    private InetAddress address;

    @BeforeEach
    void setUp() throws UnknownHostException {

        address = InetAddress.getByName("10.0.0.5");

        // flush
        ContactList.getInstance().flush();

        User.getInstance().setUsername("local");
    }

    @Test
    @DisplayName("toBuffer() → parse() round-trip works")
    void roundTripSerialization() throws InvalidMessageException {
        Message original = new Message(
                Message.Type.DISCOVER_ME,
                "hello world",
                address
        );

        byte[] buffer = original.toBuffer();
        Message parsed = Message.parse(buffer, buffer.length, address);

        assertEquals(original.getAddress(), parsed.getAddress());
        assertEquals(original.getType(), parsed.getType());
        assertEquals(original.getContent(), parsed.getContent());
        assertEquals(original.getAddress(), parsed.getAddress());
    }

    @Test
    @DisplayName("parse() with empty content works")
    void parseEmptyContent() throws InvalidMessageException {
        Message msg = new Message(Message.Type.DISCONNECT, "", address);
        byte[] buffer = msg.toBuffer();

        Message parsed = Message.parse(buffer, buffer.length, address);
        assertEquals("", parsed.getContent());
    }

    @Test
    @DisplayName("parse() throws on short buffer")
    void parseShortBufferThrows() {
        byte[] shortBuffer = new byte[1];

        assertThrows(InvalidMessageException.class,
                () -> Message.parse(shortBuffer, shortBuffer.length, address));
    }

    @Test
    @DisplayName("parse() throws on invalid type ordinal")
    void parseInvalidTypeThrows() {
        byte[] buffer = new byte[20 + 2];
        var bb = java.nio.ByteBuffer.wrap(buffer);
        bb.putInt(999); // invalid ordinal for TYPE
        bb.putChar('x');

        assertThrows(InvalidMessageException.class,
                () -> Message.parse(buffer, buffer.length, address));
    }

    @Test
    @DisplayName("isFromMe() returns true when sender is local")
    void isFromMeWhenLocal() {
        Message msg = new Message(Message.Type.NONE, "", Network.getLocalAddress());

        assertTrue(msg.isFromMe());
    }

    @Test
    @DisplayName("getSender() throws UnknownSenderException if not in ContactList")
    void getSenderThrowsWhenUnknown() {
        Message msg = new Message(Message.Type.NONE, "", null);

        assertThrows(UnknownSenderException.class, msg::getSender);
    }

    @Test
    @DisplayName("getSender() returns correct Contact when registered")
    void getSenderReturnsContactWhenKnown() throws Exception {
        ContactList.getInstance().registerContact("alice", address);

        Message msg = new Message(Message.Type.NONE, "", address);
        Contact sender = msg.getSender();

        assertEquals("alice", sender.getUsername());
        assertEquals(address, sender.getAddress());
    }

}