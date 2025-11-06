package net.chatsystem.models;

import net.chatsystem.models.exceptions.UsernameAlreadyTakenException;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ContactList {

    public static final ContactList instance = new ContactList();
    private final Map<String, Contact> contacts = new ConcurrentHashMap<>();
    private final Map<UUID, Contact> contactsByUUID = new ConcurrentHashMap<>();

    public Contact registerContact(String username, UUID uuid, InetAddress address) throws UsernameAlreadyTakenException {
        if (contacts.containsKey(username) || User.getInstance().getUsername().equals(username))
            throw new UsernameAlreadyTakenException();
        Contact contact = new Contact(username, uuid, address);
        contacts.put(username, contact);
        contactsByUUID.put(uuid, contact);
        return contact;
    }

    public Optional<Contact> getContact(String username) {
        if (!contacts.containsKey(username)) return Optional.empty();
        return Optional.of(contacts.get(username));
    }

    public Optional<Contact> getContactByUUID(UUID uuid) {
        if (!contactsByUUID.containsKey(uuid)) return Optional.empty();
        return Optional.of(contactsByUUID.get(uuid));
    }

    public static ContactList getInstance() {
        return instance;
    }

    public List<Contact> getContacts() {
        return contacts.values().stream().toList();
    }

}
