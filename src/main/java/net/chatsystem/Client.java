package net.chatsystem;


import net.chatsystem.models.User;
import net.chatsystem.network.discovery.DiscoveryServer;
import net.chatsystem.observer.OutObserver;

import java.net.SocketException;

public class Client {
    public static void main(String[] args) throws SocketException, InterruptedException {

        DiscoveryServer server = new DiscoveryServer();
        server.addObserver(new OutObserver());
        server.start();

        server.attemptLogin();

        server.join();
        System.out.println("done");

    }
}