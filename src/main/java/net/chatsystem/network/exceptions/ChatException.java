package net.chatsystem.network.exceptions;

public class ChatException extends Exception {
    public final Throwable cause;
    public ChatException(Throwable cause) {
        super();
        this.cause = cause;
    }
}
