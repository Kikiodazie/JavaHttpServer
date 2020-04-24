package com.odazie.javahttpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;


public class Main {
    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(Server.getPORT())) {
            System.out.println("Server started. \n Listening for connections on port: " + Server.getPORT() + "....\n");

            // We listen until User Halts server execution
            while (true){

                Server myServer = new Server(serverSocket.accept());
                if (Server.isVerbose()){
                    System.out.println("Connection Opened. (" + new Date() + ")");
                }

                // Create a dedicated thread to manage client Connection
                Thread thread = new Thread(myServer);
                thread.start();
            }

        }catch (IOException e){
            System.err.println("Server Connection error: " + e.getMessage());

        }
    }
}
