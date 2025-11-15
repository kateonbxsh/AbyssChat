package net.chatsystem.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User model tests")
class UserTest {

    private User user;
    private UUID uuid;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        user = new User("alice", uuid);
    }

    @Test
    @DisplayName("Constructor sets username and uuid")
    void constructorSetsFields() {
        assertEquals("alice", user.getUsername());
        assertEquals(uuid, user.getUUID());
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
        assertNotNull(singleton.getUUID());
    }

}
