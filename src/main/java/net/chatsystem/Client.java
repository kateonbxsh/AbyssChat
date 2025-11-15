package net.chatsystem;


import java.net.SocketException;

import net.chatsystem.controller.CommandLine;
import net.chatsystem.controller.LoginController;
import net.chatsystem.network.discovery.DiscoveryServer;

public class Client {

    public static class ShutdownDisconnect extends Thread {
        @Override
        public void run() {
            DiscoveryServer.getInstance().disconnect();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        DiscoveryServer server = DiscoveryServer.getInstance();
        LoginController controller = LoginController.getInstance();
        server.addObserver(controller);

        // bind the server
        try {
            server.bind();
        } catch(SocketException e) {
            CommandLine.error("Could not start app, error: {}", e.getMessage());
        }

        // add shutdown hook, in case user exits without disconnecting
        Runtime.getRuntime().addShutdownHook(new ShutdownDisconnect());

        // start threads
        controller.start();
        server.start();

        // wait for threads
        server.join();
        controller.join();

    }
}