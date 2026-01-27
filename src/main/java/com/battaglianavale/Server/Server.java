package com.battaglianavale.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import com.battaglianavale.Client.Client;

public class Server {
    public static void main(String[] args) {
        int port = 5000;
        Gioco gioco = new Gioco();
        System.out.println("Avvio del server...");

        while (true) {
            Socket socket = serverSocket.accept();
            ClientThread thread = new ClientThread(socket, gioco);
            thread.start();
            
        }
    }
}
