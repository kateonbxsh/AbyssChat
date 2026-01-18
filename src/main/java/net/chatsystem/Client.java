package net.chatsystem;


import java.io.IOException;
import java.util.List;

import net.chatsystem.controller.CommandLine;
import net.chatsystem.controller.LoginController;
import net.chatsystem.network.chat.ChatServer;
import net.chatsystem.network.discovery.DiscoveryServer;
import net.chatsystem.ui.MainFrame;

public class Client {

    public static class ShutdownDisconnect extends Thread {
        @Override
        public void run() {
            DiscoveryServer.getInstance().disconnect();
        }
    }

    public static void main(String[] args) {

        DiscoveryServer server = DiscoveryServer.getInstance();
        LoginController controller = LoginController.getInstance();
        ChatServer chat = ChatServer.getInstance();


        // if needing to run the project locally (on one computer, set a send and receive port)
        if (List.of(args).contains("--local1")) {
            DiscoveryServer.SEND_PORT = 2501;
            DiscoveryServer.RECEIVE_PORT = 2502;
        }
        if (List.of(args).contains("--local2")) {
            DiscoveryServer.SEND_PORT = 2502;
            DiscoveryServer.RECEIVE_PORT = 2501;
        }

        // bind the server
        try {
            chat.bind();
            server.bind();
        } catch(IOException e) {
            CommandLine.error("Could not start app, error: {}", e.getMessage());
        }

        // add shutdown hook, in case user exits without disconnecting
        Runtime.getRuntime().addShutdownHook(new ShutdownDisconnect());

        server.start();
        chat.start();

        // CLI mode
        if (List.of(args).contains("--cl")) {
            chat.addObserver(controller);
            server.addObserver(controller);
            controller.start();
        }
        // UI mode
        else new MainFrame();

    }
}