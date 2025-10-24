package net.chatsystem;


import net.chatsystem.models.User;
import net.chatsystem.network.discovery.DiscoveryServer;

import java.net.SocketException;

public class Client {
    public static void main(String[] args) throws SocketException, InterruptedException {

        DiscoveryServer server = new DiscoveryServer();
        server.start();

        User.getInstance().setUsername("nawfal lhmodi");
        Thread.sleep(500);
        server.attemptLogin();

        server.join();
        System.out.println("done");

    }
}