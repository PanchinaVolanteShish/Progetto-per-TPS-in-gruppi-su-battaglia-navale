package com.battaglianavale.Client;

import java.io.BufferedReader;
import java.net.Socket;

public class Client {

    public static void main (String[] args)
    {
        String host = "localhost";
        int porta = 5003;
        Gson gson = new Gson();

        try (Socket socket = new Socket(host,porta))
        {
            PrintWriter out = new PrinterWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new inputStreamReader(socket.getInputStream()));
            
        }
    }
    
}
