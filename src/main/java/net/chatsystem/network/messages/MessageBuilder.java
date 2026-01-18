package net.chatsystem.network.messages;

import net.chatsystem.models.Contact;

import java.net.InetAddress;

public class MessageBuilder {

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

    public MessageBuilder setType(Message.Type type) {
        this.type = type;
        return this;
    }

    public MessageBuilder setRecipient(Contact contact) {
        this.address = contact.getAddress();
        return this;
    }

    public Message build() {
        return new Message(type, content, address);
    }
}
