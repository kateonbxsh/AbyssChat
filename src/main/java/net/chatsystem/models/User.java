package net.chatsystem.models;

import java.util.UUID;

public class User {

    public static final User instance = new User("");
    public String username;
    public UUID uuid;

    public User(String username) {
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
}
