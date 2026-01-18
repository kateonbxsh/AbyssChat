package net.chatsystem.network.chat;

import net.chatsystem.models.Contact;
import net.chatsystem.network.exceptions.ChatException;
import net.chatsystem.network.exceptions.RecipientOfflineException;
import net.chatsystem.network.exceptions.UnknownRecipientException;
import net.chatsystem.network.messages.Message;
import net.chatsystem.network.messages.MessageBuilder;

import java.io.IOException;

public class ChatSession {

    public Contact recipient;

    public ChatSession(Contact with) {
        this.recipient = with;
    }

    public void attemptOpen() throws RecipientOfflineException {
        if (recipient.getStatus() == Contact.Status.OFFLINE) {
            throw new RecipientOfflineException();
        }
        try {
            ChatServer.getInstance().initiateChat(recipient);
        } catch(Exception e) {
            throw new RecipientOfflineException();
        }
    }

    public boolean isOpen() {
        return ChatServer.getInstance().isChatOpen(recipient);
    }

    public void send(String chat) throws UnknownRecipientException, ChatException {
        Message msg = new MessageBuilder()
                .setRecipient(this.recipient)
                .setType(Message.Type.CHAT_MESSAGE)
                .setContent(chat).build();
        try {
            ChatServer.getInstance().sendMessage(this.recipient.getAddress(), msg);
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
