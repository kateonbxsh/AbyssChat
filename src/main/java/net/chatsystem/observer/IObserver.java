package net.chatsystem.observer;

import net.chatsystem.models.Contact;
import net.chatsystem.models.User;
import net.chatsystem.network.messages.Message;

public interface IObserver {

    // general
    default void onMessage(Message message) {};

    // discovery
    default void onDiscoverContact(Contact contact) {};
    default void onUsernameTaken() {};
    default void onContactDisconnect(Contact contact) {};
    default void onContactUsernameChange(Contact contact, String oldUsername, String newUsername) {};
    default void onContactStatusUpdate(Contact contact) {}
    default void onLoggedIn(User as) {};
    default void onUsernameChanged(String newUsername) {}
    default void onStatusChanged(User.Status newStatus) {}

    // chat
    default void onChatInitiate(Contact from) {}
    default void onChatMessage(Contact from, String chat) {}
    default void onChatClose(Contact from) {}

}
