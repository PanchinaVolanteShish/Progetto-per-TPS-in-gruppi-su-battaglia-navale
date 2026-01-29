package com.battaglianavale.Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;

public class Client {

    public static void main (String[] args)
    {
        String host = "localhost";
        int porta = 5000;
        Gson gson = new Gson();

        try (Socket socket = new Socket(host, porta))
        {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
