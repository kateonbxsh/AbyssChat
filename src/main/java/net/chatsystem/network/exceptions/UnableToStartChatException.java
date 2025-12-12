package net.chatsystem.network.exceptions;

public class UnableToStartChatException extends Exception {

    public Throwable cause;

    public UnableToStartChatException(Throwable cause) {
        super();
        this.cause = cause;
    }
}
