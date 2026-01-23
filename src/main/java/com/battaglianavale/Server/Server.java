package com.battaglianavale.Server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        int port = 5000;
        Gioco gioco = new Gioco();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server avviato sulla porta " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Nuovo client connesso");

                ClientThread thread = new Thread(socket, gioco);
                thread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
