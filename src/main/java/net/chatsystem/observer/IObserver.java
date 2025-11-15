package net.chatsystem.observer;

import net.chatsystem.models.Contact;
import net.chatsystem.network.messages.Message;

public interface IObserver {

    default void onMessage(Message message) {};

    default void onDiscoverContact(Contact contact) {};

    default void onNotifyUsernameTaken() {};

    default void onContactDisconnect(Contact contact) {};

    default void onContactUsernameChange(Contact contact, String oldUsername, String newUsername) {};

}
