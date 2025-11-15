package net.chatsystem.models;

import net.chatsystem.models.exceptions.UsernameAlreadyTakenException;
import org.junit.jupiter.api.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContactList model tests")
class ContactListTest {

    private ContactList list;
    private UUID uuid1, uuid2;
    private InetAddress addr1, addr2;

    @BeforeEach
    void setUp() throws UnknownHostException {
        
        list = ContactList.getInstance();
        list.flush();

        uuid1 = UUID.randomUUID();
        uuid2 = UUID.randomUUID();
        addr1 = InetAddress.getByName("10.0.0.1"); // lambda IPs
        addr2 = InetAddress.getByName("10.0.0.2");

        // lambda user
        User.getInstance().setUsername("localUser");
    }

    @Test
    @DisplayName("registerContact adds contact and prevents duplicates")
    void registerContact() throws UsernameAlreadyTakenException {
        Contact c = list.registerContact("alice", uuid1, addr1);

        assertEquals("alice", c.getUsername());
        assertTrue(list.getContact("alice").isPresent());
        assertTrue(list.getContactByUUID(uuid1).isPresent());

        // duplicate username
        assertThrows(UsernameAlreadyTakenException.class,
                () -> list.registerContact("alice", uuid2, addr2));
    }

    @Test
    @DisplayName("registerContact rejects local username")
    void rejectLocalUsername() {
        assertThrows(UsernameAlreadyTakenException.class,
                () -> list.registerContact("localUser", uuid1, addr1));
    }

    @Test
    @DisplayName("unregisterContact removes from both maps")
    void unregisterContact() throws UsernameAlreadyTakenException {
        list.registerContact("bob", uuid1, addr1);
        Contact contact = list.getContact("bob").orElseThrow();

        list.unregisterContact(contact);

        assertFalse(list.getContact("bob").isPresent());
        assertFalse(list.getContactByUUID(uuid1).isPresent());
    }

    @Test
    @DisplayName("changeContactUsername updates maps and rejects taken names")
    void changeContactUsername() throws UsernameAlreadyTakenException {
        list.registerContact("charlie", uuid1, addr1);
        list.registerContact("diana", uuid2, addr2);

        list.changeContactUsername(uuid1, "charlie_new");

        assertFalse(list.getContact("charlie").isPresent());
        assertTrue(list.getContact("charlie_new").isPresent());
        assertEquals("charlie_new", list.getContactByUUID(uuid1).orElseThrow().getUsername());

        // trying to take an existing name
        assertThrows(UsernameAlreadyTakenException.class,
                () -> list.changeContactUsername(uuid1, "diana"));
    }

    @Test
    @DisplayName("getContacts returns immutable list of all contacts")
    void getContacts() throws UsernameAlreadyTakenException {
        list.registerContact("eve", uuid1, addr1);
        list.registerContact("frank", uuid2, addr2);

        List<Contact> all = list.getContacts();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(c -> c.getUsername().equals("eve")));
        assertTrue(all.stream().anyMatch(c -> c.getUsername().equals("frank")));

        // list is unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> all.add(null));
    }

}