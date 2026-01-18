package net.chatsystem.models;

import net.chatsystem.models.exceptions.UsernameAlreadyTakenException;

import java.net.InetAddress;
import java.util.*;

public class ContactList {

    private static final class Holder {
        private static final ContactList INSTANCE = new ContactList();
    }

    public static ContactList getInstance() {
        return Holder.INSTANCE;
    }

    private final Map<String, Contact> contactsByUsername = new HashMap<>();
    private final Map<InetAddress, Contact> contactsByIP = new HashMap<>();

    private ContactList() {}

    public synchronized Contact registerContact(String username, InetAddress address) throws UsernameAlreadyTakenException {
        
        if (username == null) throw new NullPointerException();
        if (username.isBlank()) throw new IllegalArgumentException();

        username = username.trim();

        if (User.getInstance().getUsername().equals(username)) {
            throw new UsernameAlreadyTakenException();
        }

        Contact existingByUsername = contactsByUsername.get(username);
        Contact existingByIP = contactsByIP.get(address);

        if (existingByUsername != null) {
            // username exists
            if (!existingByUsername.getAddress().equals(address)) {
                throw new UsernameAlreadyTakenException();
            }
            // same IP & username → just mark online
            existingByUsername.setStatus(User.Status.ONLINE);
            return existingByUsername;
        }

        if (existingByIP != null) {
            // IP exists, new username → update username
            contactsByUsername.remove(existingByIP.getUsername()); // remove old mapping
            existingByIP.setUsername(username);
            contactsByUsername.put(username, existingByIP);
            return existingByIP;
        }

        // completely new contact
        Contact contact = new Contact(username, address);
        contactsByUsername.put(username, contact);
        contactsByIP.put(address, contact);
        return contact;
    }

    public synchronized void changeContactUsername(InetAddress address, String newUsername) throws UsernameAlreadyTakenException {
        if (User.getInstance().getUsername().equals(newUsername)) throw new UsernameAlreadyTakenException();
        if (contactsByUsername.containsKey(newUsername)) throw new UsernameAlreadyTakenException();

        Contact contact = contactsByIP.get(address);
        if (contact == null) throw new IllegalArgumentException("No contact with given IP");

        contactsByUsername.remove(contact.getUsername());
        contact.setUsername(newUsername);
        contactsByUsername.put(newUsername, contact);
    }

    public synchronized Optional<Contact> getContact(String username) {
        return Optional.ofNullable(contactsByUsername.get(username));
    }

    public synchronized Optional<Contact> getContactByIP(InetAddress address) {
        return Optional.ofNullable(contactsByIP.get(address));
    }

    public synchronized List<Contact> getContacts() {
        return List.copyOf(contactsByUsername.values());
    }

    public synchronized void flush() {
        contactsByUsername.clear();
        contactsByIP.clear();
    }
}
