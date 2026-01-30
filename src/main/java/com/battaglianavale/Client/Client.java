package com.battaglianavale.Client;

import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== CLIENT BATTLAGLIA NAVALE ===");
        System.out.print("Inserisci il tuo nome: ");
        String nome = scanner.nextLine();
        
        // Usa valori di default o parametri da riga di comando
        String host = "localhost";
        int porta = 5000;
        
        if (args.length >= 1) host = args[0];
        if (args.length >= 2) porta = Integer.parseInt(args[1]);
        
        System.out.println("Connessione a " + host + ":" + porta + "...");
        
        Gioco gioco = new Gioco(host, porta, nome);
        gioco.start();
        
        scanner.close();
    }
}