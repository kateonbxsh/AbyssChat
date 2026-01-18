package net.chatsystem.network.exceptions;

import java.net.InetAddress;

public class UnknownRecipientException extends Exception {
    public final InetAddress who;
    public UnknownRecipientException(InetAddress who) {
        super();
        this.who = who;
    }
}
