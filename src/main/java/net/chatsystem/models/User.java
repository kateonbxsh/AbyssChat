package net.chatsystem.models;

import java.util.UUID;

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
}
