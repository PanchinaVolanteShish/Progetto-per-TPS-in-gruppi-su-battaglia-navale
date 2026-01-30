package com.battaglianavale.Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.battaglianavale.Server.Messaggio;
import com.battaglianavale.Server.TipoMessaggio;

public class Gioco {
    private Comunicazione comunicazione;
    private String nomeGiocatore;
    private int playerId;
    private char[][] miaGriglia;
    private char[][] grigliaNemico;
    private boolean mioTurno;
    private boolean inGioco;
    private boolean naviPosizionate;
    private Scanner scanner;
    
    // Dimensioni griglia
    private static final int DIMENSIONE = 10;
    
    public Gioco(String host, int porta, String nome) {
        this.nomeGiocatore = nome;
        this.miaGriglia = new char[DIMENSIONE][DIMENSIONE];
        this.grigliaNemico = new char[DIMENSIONE][DIMENSIONE];
        this.scanner = new Scanner(System.in);
        this.inGioco = true;
        this.naviPosizionate = false;
        this.mioTurno = false;
        
        // Inizializza le griglie
        for (int i = 0; i < DIMENSIONE; i++) {
            for (int j = 0; j < DIMENSIONE; j++) {
                miaGriglia[i][j] = '~'; // Acqua
                grigliaNemico[i][j] = '~'; // Acqua
            }
        }
        
        try {
            comunicazione = new Comunicazione(host, porta, this);
            comunicazione.inviaJoin(nome);
        } catch (Exception e) {
            System.err.println("Errore di connessione: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public void start() {
        System.out.println("=== BATTLAGLIA NAVALE ===");
        System.out.println("Giocatore: " + nomeGiocatore);
        
        // Thread principale rimane in ascolto di input utente
        while (inGioco) {
            if (mioTurno && naviPosizionate) {
                gestisciTurno();
            }
            
            try {
                Thread.sleep(100); // Evita uso eccessivo CPU
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    // Metodo chiamato quando è il turno del giocatore
    private void gestisciTurno() {
        stampaGriglie();
        System.out.println("\n--- È IL TUO TURNO ---");
        System.out.print("Inserisci coordinate attacco (x y, 0-9): ");
        
        try {
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            scanner.nextLine(); // Consuma newline
            
            if (x < 0 || x >= DIMENSIONE || y < 0 || y >= DIMENSIONE) {
                System.out.println("Coordinate non valide! Usa numeri 0-9.");
                return;
            }
            
            // Invia attacco al server
            comunicazione.inviaAttacco(x, y);
            mioTurno = false; // Attendi risposta dal server
            
        } catch (Exception e) {
            System.out.println("Input non valido!");
            scanner.nextLine(); // Pulisci buffer
        }
    }
    
    // Metodo per posizionare navi (interattivo)
    public void posizionaNaviInterattivo() {
        System.out.println("\n=== POSIZIONAMENTO NAVI ===");
        System.out.println("Posiziona le tue navi sulla griglia 10x10");
        System.out.println("Coordinate vanno da 0 a 9");
        
        List<Map<String, Object>> navi = new ArrayList<>();
        
        // Nave 1: Portaerei (5 celle)
        System.out.println("\n1. Portaerei (5 celle)");
        navi.add(posizionaNaveInterattiva("Portaerei", 5));
        
        // Nave 2: Corazzata (4 celle)
        System.out.println("\n2. Corazzata (4 celle)");
        navi.add(posizionaNaveInterattiva("Corazzata", 4));
        
        // Nave 3: Cacciatorpediniere (3 celle)
        System.out.println("\n3. Cacciatorpediniere (3 celle)");
        navi.add(posizionaNaveInterattiva("Cacciatorpediniere", 3));
        
        // Nave 4: Sottomarino (2 celle)
        System.out.println("\n4. Sottomarino (2 celle)");
        navi.add(posizionaNaveInterattiva("Sottomarino", 2));
        
        // Invia posizionamento al server
        Map<String, Object> payload = new HashMap<>();
        payload.put("ships", navi);
        comunicazione.inviaPosizionamentoNavi(payload);
    }
    
    private Map<String, Object> posizionaNaveInterattiva(String nome, int dimensione) {
        stampaGrigliaPersonale();
        System.out.println("Posizionamento " + nome + " (" + dimensione + " celle)");
        
        List<Map<String, Object>> posizioni = new ArrayList<>();
        
        for (int i = 0; i < dimensione; i++) {
            while (true) {
                System.out.print("Cella " + (i+1) + "/" + dimensione + " (x y): ");
                try {
                    int x = scanner.nextInt();
                    int y = scanner.nextInt();
                    scanner.nextLine();
                    
                    if (x < 0 || x >= DIMENSIONE || y < 0 || y >= DIMENSIONE) {
                        System.out.println("Coordinate non valide! 0-9.");
                        continue;
                    }
                    
                    if (miaGriglia[x][y] != '~') {
                        System.out.println("Cella già occupata!");
                        continue;
                    }
                    
                    // Posiziona nave nella griglia locale
                    miaGriglia[x][y] = 'B';
                    
                    Map<String, Object> pos = new HashMap<>();
                    pos.put("x", x);
                    pos.put("y", y);
                    posizioni.add(pos);
                    
                    break;
                    
                } catch (Exception e) {
                    System.out.println("Input non valido!");
                    scanner.nextLine();
                }
            }
            stampaGrigliaPersonale();
        }
        
        Map<String, Object> nave = new HashMap<>();
        nave.put("name", nome);
        nave.put("positions", posizioni);
        
        return nave;
    }
    
    // Metodi chiamati dalla comunicazione per aggiornare lo stato
    public void setPlayerId(int id) {
        this.playerId = id;
        System.out.println("Sei il giocatore " + id);
    }
    
    public void setNaviPosizionate(boolean posizionate) {
        this.naviPosizionate = posizionate;
    }
    
    public void setMioTurno(boolean turno) {
        this.mioTurno = turno;
        if (turno && inGioco) {
            System.out.println("\n--- È IL TUO TURNO! ---");
        }
    }
    
    public void aggiornaRisultatoAttacco(int x, int y, String risultato, boolean mioAttacco) {
        char[][] grigliaDaAggiornare = mioAttacco ? grigliaNemico : miaGriglia;
        
        switch (risultato) {
            case "HIT":
                grigliaDaAggiornare[x][y] = 'X';
                System.out.println("Colpito! (" + x + "," + y + ")");
                break;
            case "SUNK":
                grigliaDaAggiornare[x][y] = 'X';
                System.out.println("NAVE AFFONDATA! (" + x + "," + y + ")");
                break;
            case "MISS":
                grigliaDaAggiornare[x][y] = 'O';
                System.out.println("Acqua. (" + x + "," + y + ")");
                break;
            case "ALREADY_ATTACKED":
                System.out.println("Cella già attaccata! (" + x + "," + y + ")");
                break;
        }
        
        stampaGriglie();
    }
    
    public void finePartita(String vincitore) {
        System.out.println("\n=== PARTITA TERMINATA ===");
        System.out.println("VINCITORE: " + vincitore);
        if (vincitore.equals(nomeGiocatore)) {
            System.out.println("COMPLIMENTI! HAI VINTO!");
        } else {
            System.out.println("Hai perso. Ritenta!");
        }
        inGioco = false;
        comunicazione.chiudi();
        scanner.close();
    }
    
    public void mostraErrore(String messaggio) {
        System.out.println("ERRORE: " + messaggio);
    }
    
    // Utility
    private void stampaGriglie() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("TUA GRIGLIA");
        stampaGrigliaPersonale();
        
        System.out.println("\nGRIGLIA NEMICA");
        stampaGrigliaNemica();
        System.out.println("=".repeat(50));
    }
    
    private void stampaGrigliaPersonale() {
        stampaGriglia(miaGriglia, true);
    }
    
    private void stampaGrigliaNemica() {
        stampaGriglia(grigliaNemico, false);
    }
    
    private void stampaGriglia(char[][] griglia, boolean mostraNavi) {
        System.out.print("  ");
        for (int i = 0; i < DIMENSIONE; i++) {
            System.out.print(i + " ");
        }
        System.out.println();
        
        for (int i = 0; i < DIMENSIONE; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < DIMENSIONE; j++) {
                char c = griglia[i][j];
                if (!mostraNavi && c == 'B') {
                    System.out.print("~ "); // Nascondi navi nemiche
                } else {
                    System.out.print(c + " ");
                }
            }
            System.out.println();
        }
    }
    
    public void chiudi() {
        inGioco = false;
        if (comunicazione != null) {
            comunicazione.chiudi();
        }
        if (scanner != null) {
            scanner.close();
        }
    }
}