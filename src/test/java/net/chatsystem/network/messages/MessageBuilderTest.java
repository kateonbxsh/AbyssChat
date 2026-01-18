package net.chatsystem.network.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.chatsystem.models.Contact;
import net.chatsystem.models.User;

@DisplayName("MessageBuilder tests")
class MessageBuilderTest {

    private InetAddress address;

    @BeforeEach
    void setUp() throws UnknownHostException {
        
        address = InetAddress.getByName("192.168.1.100");

        // ensure the singleton user has a known UUID
        User.getInstance().setUsername("me");
        address = User.getInstance().getAddress();
    }

    @Test
    @DisplayName("build() uses default values and overrides correctly")
    void buildWithDefaultsAndOverrides() {
        Message msg = new MessageBuilder()
                .setType(Message.Type.DISCOVER_ME)
                .setContent("hello")
                .setAddress(address)
                .build();

        assertEquals(address, msg.getAddress());
        assertEquals(Message.Type.DISCOVER_ME, msg.getType());
        assertEquals("hello", msg.getContent());
        assertEquals(address, msg.getAddress());
    }

    @Test
    @DisplayName("setRecipient() copies address from Contact")
    void setRecipientCopiesAddress() throws UnknownHostException {
        Contact contact = new Contact("bob", address);
        Message msg = new MessageBuilder()
                .setRecipient(contact)
                .setType(Message.Type.CHANGE_USERNAME_REQUEST)
                .build();

        assertEquals(address, msg.getAddress());
        assertEquals(Message.Type.CHANGE_USERNAME_REQUEST, msg.getType());
    }

}