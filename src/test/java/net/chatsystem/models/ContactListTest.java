package net.chatsystem.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.chatsystem.models.exceptions.UsernameAlreadyTakenException;

@DisplayName("ContactList model tests")
class ContactListTest {

    private ContactList list;
    private InetAddress addr1, addr2;

    @BeforeEach
    void setUp() throws UnknownHostException {
        list = ContactList.getInstance();
        list.flush();

        addr1 = InetAddress.getByName("10.0.0.1");
        addr2 = InetAddress.getByName("10.0.0.2");

        // set local user
        User.getInstance().setUsername("localUser");
    }

    @Test
    @DisplayName("registerContact adds contact and prevents duplicates by username")
    void registerContact() throws UsernameAlreadyTakenException {
        Contact c = list.registerContact("alice", addr1);

        assertEquals("alice", c.getUsername());
        assertTrue(list.getContact("alice").isPresent());
        assertTrue(list.getContactByIP(addr1).isPresent());

        // duplicate username on a different IP should fail
        assertThrows(UsernameAlreadyTakenException.class,
                () -> list.registerContact("alice", addr2));

        // re-register same username + IP → should mark online
        Contact same = list.registerContact("alice", addr1);
        assertEquals(User.Status.ONLINE, same.getStatus());
    }

    @Test
    @DisplayName("registerContact rejects local username")
    void rejectLocalUsername() {
        assertThrows(UsernameAlreadyTakenException.class,
                () -> list.registerContact("localUser", addr1));
    }

    @Test
    @DisplayName("registerContact with existing IP updates username")
    void registerContactUpdatesUsername() throws UsernameAlreadyTakenException {
        // first registration
        Contact c1 = list.registerContact("bob", addr1);
        assertEquals("bob", c1.getUsername());

        // same IP, new username → should update
        Contact updated = list.registerContact("bob_new", addr1);
        assertEquals("bob_new", updated.getUsername());
        assertTrue(list.getContact("bob").isEmpty());
        assertTrue(list.getContact("bob_new").isPresent());
        assertEquals(updated, list.getContactByIP(addr1).orElseThrow());
    }

    @Test
    @DisplayName("changeContactUsername updates maps and rejects taken names or non-existent IPs")
    void changeContactUsername() throws UsernameAlreadyTakenException, UnknownHostException {
        list.registerContact("charlie", addr1);
        list.registerContact("diana", addr2);

        // valid username change
        list.changeContactUsername(addr1, "charlie_new");
        assertFalse(list.getContact("charlie").isPresent());
        assertTrue(list.getContact("charlie_new").isPresent());
        assertEquals("charlie_new", list.getContactByIP(addr1).orElseThrow().getUsername());

        // trying to take an existing username
        assertThrows(UsernameAlreadyTakenException.class,
                () -> list.changeContactUsername(addr1, "diana"));

        // changing username for non-existent IP
        InetAddress fakeAddr = InetAddress.getByName("10.0.0.99");
        assertThrows(IllegalArgumentException.class,
                () -> list.changeContactUsername(fakeAddr, "someone"));
    }

    @Test
    @DisplayName("getContacts returns snapshot list")
    void getContacts() throws UsernameAlreadyTakenException {
        list.registerContact("eve", addr1);
        list.registerContact("frank", addr2);

        List<Contact> all = list.getContacts();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(c -> c.getUsername().equals("eve")));
        assertTrue(all.stream().anyMatch(c -> c.getUsername().equals("frank")));

        // list is unmodifiable (snapshot)
        assertThrows(UnsupportedOperationException.class, () -> all.add(null));
    }

    @Test
    @DisplayName("flush clears all contacts")
    void flushClearsContacts() throws UsernameAlreadyTakenException {
        list.registerContact("george", addr1);
        list.registerContact("hannah", addr2);

        list.flush();
        assertTrue(list.getContacts().isEmpty());
        assertTrue(list.getContact("george").isEmpty());
        assertTrue(list.getContactByIP(addr1).isEmpty());
    }

    @Test
    @DisplayName("null or blank usernames are rejected")
    void rejectNullOrBlankUsernames() {
        assertThrows(NullPointerException.class,
                () -> list.registerContact(null, addr1));

        assertThrows(IllegalArgumentException.class,
                () -> list.registerContact("   ", addr1));
    }
}
