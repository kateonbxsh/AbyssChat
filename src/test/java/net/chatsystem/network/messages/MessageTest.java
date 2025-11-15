package net.chatsystem.network.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.chatsystem.models.Contact;
import net.chatsystem.models.ContactList;
import net.chatsystem.models.User;
import net.chatsystem.network.exceptions.InvalidMessageException;
import net.chatsystem.network.exceptions.UnknownSenderException;

@DisplayName("Message serialization & parsing tests")
class MessageTest {

    private UUID senderUuid;
    private InetAddress address;

    @BeforeEach
    void setUp() throws UnknownHostException {
        senderUuid = UUID.randomUUID();
        address = InetAddress.getByName("10.0.0.5");

        // flush
        ContactList.getInstance().flush();

        User.getInstance().setUsername("local");
    }

    @Test
    @DisplayName("toBuffer() → parse() round-trip works")
    void roundTripSerialization() throws InvalidMessageException {
        Message original = new Message(
                senderUuid,
                Message.Type.DISCOVER_ME,
                "hello world",
                address
        );

        byte[] buffer = original.toBuffer();
        Message parsed = Message.parse(buffer, buffer.length, address);

        assertEquals(original.getSenderUUID(), parsed.getSenderUUID());
        assertEquals(original.getType(), parsed.getType());
        assertEquals(original.getContent(), parsed.getContent());
        assertEquals(original.getAddress(), parsed.getAddress());
    }

    @Test
    @DisplayName("parse() with empty content works")
    void parseEmptyContent() throws InvalidMessageException {
        Message msg = new Message(senderUuid, Message.Type.DISCONNECT, "", address);
        byte[] buffer = msg.toBuffer();

        Message parsed = Message.parse(buffer, buffer.length, address);
        assertEquals("", parsed.getContent());
    }

    @Test
    @DisplayName("parse() throws on short buffer")
    void parseShortBufferThrows() {
        byte[] shortBuffer = new byte[10]; // less than 16+4 = 20 bytes

        assertThrows(InvalidMessageException.class,
                () -> Message.parse(shortBuffer, shortBuffer.length, address));
    }

    @Test
    @DisplayName("parse() throws on invalid type ordinal")
    void parseInvalidTypeThrows() {
        byte[] buffer = new byte[20 + 2];
        var bb = java.nio.ByteBuffer.wrap(buffer);
        bb.putLong(senderUuid.getMostSignificantBits());
        bb.putLong(senderUuid.getLeastSignificantBits());
        bb.putInt(999); // invalid ordinal for TYPE
        bb.putChar('x');

        assertThrows(InvalidMessageException.class,
                () -> Message.parse(buffer, buffer.length, address));
    }

    @Test
    @DisplayName("isFromMe() returns true when sender is local")
    void isFromMeWhenLocal() {
        setUserUUID(senderUuid); // make local user match sender
        Message msg = new Message(senderUuid, Message.Type.NONE, "", null);

        assertTrue(msg.isFromMe());
    }

    @Test
    @DisplayName("getSender() throws UnknownSenderException if not in ContactList")
    void getSenderThrowsWhenUnknown() {
        Message msg = new Message(UUID.randomUUID(), Message.Type.NONE, "", null);

        assertThrows(UnknownSenderException.class, msg::getSender);
    }

    @Test
    @DisplayName("getSender() returns correct Contact when registered")
    void getSenderReturnsContactWhenKnown() throws Exception {
        ContactList.getInstance().registerContact("alice", senderUuid, address);

        Message msg = new Message(senderUuid, Message.Type.NONE, "", null);
        Contact sender = msg.getSender();

        assertEquals("alice", sender.getUsername());
        assertEquals(senderUuid, sender.getUUID());
        assertEquals(address, sender.getRemoteAddress());
    }

    
    // again, make sure the uuid is set manually
    private void setUserUUID(UUID uuid) {
        try {
            var field = User.class.getDeclaredField("uuid");
            field.setAccessible(true);
            field.set(User.getInstance(), uuid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}