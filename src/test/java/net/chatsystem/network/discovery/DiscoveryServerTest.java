package net.chatsystem.network.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.chatsystem.models.User;
import net.chatsystem.network.messages.Message;
import net.chatsystem.network.messages.MessageBuilder;

@DisplayName("DiscoveryServer - Simple Fake-Socket Tests")
class DiscoveryServerTest {

    private DiscoveryServer server;
    private FakeDatagramSocket fakeSocket;
    private UUID localUuid;

    @BeforeEach
    void setUp() throws Exception {
        
        // set up local user
        localUuid = UUID.randomUUID();
        Field uuidField = User.class.getDeclaredField("uuid");
        uuidField.setAccessible(true);
        uuidField.set(User.getInstance(), localUuid);
        User.getInstance().setUsername("me");

        // inject a fake socket
        fakeSocket = new FakeDatagramSocket();
        server = DiscoveryServer.getInstance();
        Field socketField = DiscoveryServer.class.getDeclaredField("socket");
        socketField.setAccessible(true);
        socketField.set(server, fakeSocket);
    }

    @Test
    @DisplayName("attemptLogin() sends DISCOVER_ME broadcast")
    void attemptLoginSendsDiscoverMe() {
        server.attemptLogin();

        // intercept message
        List<DatagramPacket> sent = fakeSocket.getSent();
        assertEquals(1, sent.size());

        // parse it and make sure it's correct
        Message msg = parse(sent.get(0));
        assertEquals(Message.Type.DISCOVER_ME, msg.getType());
        assertEquals("me", msg.getContent());
        assertEquals(DiscoveryServer.BROADCAST_ADDRESS, msg.getAddress());
        assertEquals(localUuid, msg.getSenderUUID());
    }

    @Test
    @DisplayName("changeUsername() sends CHANGE_USERNAME_REQUEST")
    void changeUsernameSendsRequest() {
        server.changeUsername("newname");

        List<DatagramPacket> sent = fakeSocket.getSent();
        assertEquals(1, sent.size());

        Message msg = parse(sent.get(0));
        assertEquals(Message.Type.CHANGE_USERNAME_REQUEST, msg.getType());
        assertEquals("newname", msg.getContent());
        assertEquals(DiscoveryServer.BROADCAST_ADDRESS, msg.getAddress());
    }

    @Test
    @DisplayName("disconnect() sends DISCONNECT broadcast")
    void disconnectSendsDisconnect() {
        server.disconnect();

        List<DatagramPacket> sent = fakeSocket.getSent();
        assertEquals(1, sent.size());

        Message msg = parse(sent.get(0));
        assertEquals(Message.Type.DISCONNECT, msg.getType());
        assertEquals(DiscoveryServer.BROADCAST_ADDRESS, msg.getAddress());
    }

    @Test
    @DisplayName("handleMessage() ignores own messages")
    void ignoresOwnMessage() {
        Message self = new MessageBuilder()
                .setType(Message.Type.DISCOVER_ME)
                .setContent("me")
                .setSenderUUID(localUuid)
                .setAddress(InetAddress.getLoopbackAddress())
                .build();

        // should not throw, and shouldn't send anything because it's an own message
        server.handleMessage(self);
        assertTrue(fakeSocket.getSent().isEmpty());
    }

    /**
     *  FakeDatagramSocket mimicks the behavior of a datagram socket
     *  so we can catch sent packets
     */
    static class FakeDatagramSocket extends DatagramSocket {
        private final List<DatagramPacket> sent = new ArrayList<>();

        FakeDatagramSocket() throws java.net.SocketException {}

        @Override
        public void send(DatagramPacket p) {
            byte[] copy = new byte[p.getLength()];
            System.arraycopy(p.getData(), p.getOffset(), copy, 0, p.getLength());
            sent.add(new DatagramPacket(copy, copy.length, p.getAddress(), p.getPort()));
        }

        List<DatagramPacket> getSent() { return sent; }
    }

    // helper method to parse packets :P
    private Message parse(DatagramPacket p) {
        try {
            return Message.parse(p.getData(), p.getLength(), p.getAddress());
        } catch (Exception e) {
            throw new RuntimeException("Parse failed", e);
        }
    }
}