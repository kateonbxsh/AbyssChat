package net.chatsystem;


import net.chatsystem.controller.LoginController;
import net.chatsystem.network.discovery.DiscoveryServer;

public class Client {
    public static void main(String[] args) throws InterruptedException {

        DiscoveryServer server = DiscoveryServer.getInstance();
        LoginController controller = LoginController.getInstance();
        server.addObserver(controller);
        controller.start();
        server.start();

        server.join();

    }
}