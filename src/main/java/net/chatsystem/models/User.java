package net.chatsystem.models;

import java.net.InetAddress;

import net.chatsystem.controller.CommandLine;
import net.chatsystem.network.Network;

public class User {

    public enum Status {
        ONLINE,
        DO_NOT_DISTURB,
        AWAY,
        OFFLINE
    }

    public static String getStatusName(Status status, boolean me) {
        return switch (status) {
            case ONLINE -> "ONLINE";
            case DO_NOT_DISTURB -> "DO NOT DISTURB";
            case AWAY -> "AWAY";
            case OFFLINE -> me ? "INVISIBLE" : "OFFLINE";
        };
    }

    // Holder class is thread-safe by design (afaiu)
    private static final class UserHolder {
        public static final User instance = new User("");
    }

    public static User getInstance() {
        return UserHolder.instance;
    }

    public String username;

    private Status status = Status.ONLINE;

    public synchronized Status getStatus() {
        return status;
    }

    public synchronized void setStatus(Status status) {
        this.status = status;
    }

    public User(String username) {
        this.username = username;
    }

    public synchronized void setUsername(String username) {
        this.username = username;
    }

    public synchronized String getUsername() {
        return this.username;
    }

    public synchronized InetAddress getAddress() {
        return Network.getLocalAddress();
    }

    public synchronized String getPrintableName() {
        String IP = getAddress().toString();
        return CommandLine.FG_WHITE + getUsername() + CommandLine.FG_CYAN + "@" + IP;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof User u) {
            return this.getAddress().equals(u.getAddress());
        }
        return false;
    }

}
