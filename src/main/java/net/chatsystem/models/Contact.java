package net.chatsystem.models;

import java.net.InetAddress;
import java.util.UUID;

public class Contact extends User {

    private final InetAddress remoteAddress;

    public Contact(String username, UUID uuid, InetAddress address) {
        super(username, uuid);
        remoteAddress = address;
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }
}
