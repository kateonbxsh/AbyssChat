package net.chatsystem.observer;

import net.chatsystem.models.Contact;
import net.chatsystem.network.messages.Message;

public class OutObserver implements IObserver {

    @Override
    public void onMessage(Message message) {
        System.out.println("Received " + message.getType() + " from " + message.getAddress());
    }

    @Override
    public void onDiscoverContact(Contact contact) {
        System.out.println("User joined " + contact.getUsername());
    }

    @Override
    public void onNotifyUsernameTaken() {
        System.out.println("BAD USERNAME");
    }
}
