package net.chatsystem.network.chat;

import net.chatsystem.models.Contact;
import net.chatsystem.network.exceptions.ChatException;
import net.chatsystem.network.exceptions.UnableToStartChatException;
import net.chatsystem.network.exceptions.UnknownRecipientException;
import net.chatsystem.network.messages.Message;
import net.chatsystem.network.messages.MessageBuilder;
import net.chatsystem.observer.IObserver;

import java.io.IOException;

public class Chat {

    public Contact recipient;

    public Chat(Contact with) throws UnableToStartChatException, UnknownRecipientException {
        this.recipient = with;
        ChatServer.getInstance().initiateChat(with);
    }

    public void send(String chat) throws UnknownRecipientException, ChatException {
        Message msg = new MessageBuilder()
                .setRecipient(this.recipient)
                .setType(Message.Type.CHAT_MESSAGE)
                .setContent(chat).build();
        try {
            ChatServer.getInstance().sendMessage(this.recipient.getUUID(), msg);
        } catch(IOException exception) {
            throw new ChatException(exception);
        }
    }

    public void close() throws UnknownRecipientException, ChatException {
        try {
            ChatServer.getInstance().stopChat(this.recipient);
        } catch(IOException ioException) {
            throw new ChatException(ioException);
        }
    }

}
