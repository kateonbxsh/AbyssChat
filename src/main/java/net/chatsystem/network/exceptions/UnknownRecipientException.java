package net.chatsystem.network.exceptions;

import net.chatsystem.models.User;

import java.util.UUID;

public class UnknownRecipientException extends Exception {
    public final UUID who;
    public UnknownRecipientException(UUID who) {
        super();
        this.who = who;
    }
}
