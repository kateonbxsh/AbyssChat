package net.chatsystem.models;

import java.net.InetAddress;

public class Contact extends User {

    private final InetAddress remoteAddress;

    public Contact(String username, InetAddress address) {
        super(username);
        remoteAddress = address;
    }

    @Override
    public InetAddress getAddress() {
        return remoteAddress;
    }

}
