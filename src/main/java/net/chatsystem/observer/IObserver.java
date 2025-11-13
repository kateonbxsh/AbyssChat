package net.chatsystem.observer;

import net.chatsystem.models.Contact;
import net.chatsystem.network.messages.Message;

public interface IObserver {

    void onMessage(Message message);

    void onDiscoverContact(Contact contact);

    void onNotifyUsernameTaken();

    void onContactDisconnect(Contact contact);

    void onContactUsernameChange(Contact contact, String oldUsername, String newUsername);

}
