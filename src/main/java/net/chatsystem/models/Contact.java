package net.chatsystem.models;

import java.net.InetAddress;

public class Contact extends User {

    private InetAddress remoteAddress;

    public Contact(String username, InetAddress address) {
        super(username);
        remoteAddress = address;
    }
}
