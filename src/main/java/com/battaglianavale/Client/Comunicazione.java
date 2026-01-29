package com.battaglianavale.Client;

import com.battaglianavale.Server.Messaggio;
import java.io.*;
import java.net.Socket;

public class Comunicazione {
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Comunicazione(Socket socket) {
        try {
            // È importante creare prima l'output stream e fare il flush
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Invia un messaggio al server
    public void invia(Messaggio messaggio) {
        try {
            out.writeObject(messaggio);
            out.flush();
        } catch (IOException e) {
            System.err.println("Errore nell'invio del messaggio: " + e.getMessage());
        }
    }

    // Riceve un messaggio dal server (bloccante)
    public Messaggio ricevi() {
        try {
            return (Messaggio) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nella ricezione: " + e.getMessage());
            return null;
        }
    }
}
