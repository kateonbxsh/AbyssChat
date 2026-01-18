package net.chatsystem.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.InetAddress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.chatsystem.network.Network;

@DisplayName("User model tests")
class UserTest {

    private User user;
    private InetAddress address;

    @BeforeEach
    void setUp() {
        user = new User("alice");
        address = Network.getLocalAddress();
    }

    @Test
    @DisplayName("Constructor sets username and uuid")
    void constructorSetsFields() {
        assertEquals("alice", user.getUsername());
        assertEquals(address, user.getAddress());
    }

    @Test
    @DisplayName("setUsername updates username")
    void setUsernameWorks() {
        user.setUsername("bob");
        assertEquals("bob", user.getUsername());
    }

    @Test
    @DisplayName("getInstance returns the same singleton")
    void singletonInstance() {
        User singleton = User.getInstance();
        assertSame(singleton, User.getInstance());
        assertNotNull(singleton.getAddress());
    }

}
