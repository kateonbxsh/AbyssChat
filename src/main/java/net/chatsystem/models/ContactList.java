package net.chatsystem.models;

import net.chatsystem.models.exceptions.UsernameAlreadyTakenException;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ContactList {

    public static final ContactList instance = new ContactList();
    private final Map<String, Contact> contacts = new ConcurrentHashMap<>();

    public void registerContact(String username, InetAddress address) throws UsernameAlreadyTakenException {
        if (contacts.containsKey(username)) throw new UsernameAlreadyTakenException();
        if (User.getInstance().getUsername().equals(username)) throw new UsernameAlreadyTakenException();
        Contact contact = new Contact(username, address);
        contacts.put(username, contact);
    }

    public Optional<Contact> getContact(String username) {
        if (!contacts.containsKey(username)) return Optional.empty();
        return Optional.of(contacts.get(username));
    }

    public static ContactList getInstance() {
        return instance;
    }

}
