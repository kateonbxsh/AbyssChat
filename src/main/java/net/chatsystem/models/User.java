package net.chatsystem.models;

import java.util.UUID;

import net.chatsystem.controller.CommandLine;

public class User {

    public static final User instance = new User("", UUID.randomUUID());
    public String username;
    private final UUID uuid;

    public User(String username, UUID uuid) {
        this.uuid = uuid;
        this.username = username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public static User getInstance() {
        return instance;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getPrintableName() {
        String ID = getUUID().toString().subSequence(0, 6).toString().toUpperCase();
        return CommandLine.RESET + getUsername() + CommandLine.FG_CYAN + "#" + ID;
    }
}
