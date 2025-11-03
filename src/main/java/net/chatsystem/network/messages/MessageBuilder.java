package net.chatsystem.network.messages;

import net.chatsystem.models.Contact;
import net.chatsystem.models.User;

import java.net.InetAddress;
import java.util.UUID;

public class MessageBuilder {

    private UUID senderUUID = User.getInstance().getUUID();
    private Message.Type type = Message.Type.NONE;
    private String content = "";
    private InetAddress address;

    public MessageBuilder setAddress(InetAddress address) {
        this.address = address;
        return this;
    }

    public MessageBuilder setContent(String content) {
        this.content = content;
        return this;
    }

    public MessageBuilder setSenderUUID(UUID senderUUID) {
        this.senderUUID = senderUUID;
        return this;
    }

    public MessageBuilder setType(Message.Type type) {
        this.type = type;
        return this;
    }

    public MessageBuilder setRecipient(Contact contact) {
        this.address = contact.getRemoteAddress();
        return this;
    }

    public Message build() {
        return new Message(senderUUID, type, content, address);
    }
}
