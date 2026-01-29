package com.battaglianavale.Server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        int port = 5000;
        Gioco gioco = new Gioco();
        System.out.println("Avvio del server...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientThread thread = new ClientThread(socket, gioco);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
