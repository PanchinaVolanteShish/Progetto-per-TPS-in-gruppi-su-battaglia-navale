package com.battaglianavale.Client;

import com.battaglianavale.Server.*;
import java.util.*;

public class Gioco {
    private Griglia miaGriglia;
    private char[][] grigliaAvversario; // Per segnare dove abbiamo sparato noi
    private Comunicazione comm;
    private Scanner scanner;
    private boolean inCorso;

    public Gioco(Comunicazione comm) {
        this.comm = comm;
        this.miaGriglia = new Griglia();
        this.grigliaAvversario = new char[10][10];
        this.scanner = new Scanner(System.in);
        this.inCorso = true;

        // Inizializza griglia avversaria visiva
        for (int i = 0; i < 10; i++) 
            for (int j = 0; j < 10; j++) grigliaAvversario[i][j] = '?';
    }

    public void start() {
        System.out.println("In attesa di altri giocatori...");
        
        while (inCorso) {
            Messaggio m = comm.ricevi();
            if (m == null) break;

            switch (m.getType()) {
                case JOIN_OK:
                    System.out.println("Entrato in partita! In attesa dell'inizio...");
                    break;
                
                case PLACE_SHIPS:
                    posizionaNavi();
                    break;

                case TURN_CHANGE:
                    boolean mioTurno = (boolean) m.getPayload();
                    if (mioTurno) {
                        eseguiAttacco();
                    } else {
                        System.out.println("Turno dell'avversario. Attendi...");
                    }
                    break;

                case ATTACK_RESULT:
                    String risultato = (String) m.getPayload();
                    System.out.println("Risultato tuo attacco: " + risultato);
                    // Qui potresti aggiornare grigliaAvversario se il server ti manda le coordinate
                    break;

                case INCOMING_ATTACK:
                    // Il server ci avvisa che siamo stati colpiti, la classe Griglia lo gestisce
                    Map<String, Integer> coord = (Map<String, Integer>) m.getPayload();
                    String esito = miaGriglia.Attacco(coord.get("x"), coord.get("y"));
                    System.out.println("L'avversario ha sparato in " + coord.get("x") + "," + coord.get("y") + ": " + esito);
                    break;

                case GAME_OVER:
                    System.out.println("Partita Terminata! " + m.getPayload());
                    inCorso = false;
                    break;
            }
        }
    }

    private void posizionaNavi() {
        System.out.println("Configurazione navi in corso...");
        // Qui dovresti creare il payload richiesto da Griglia.PosizionaNave
        // Per brevità ne creiamo uno d'esempio, ma dovresti chiedere input all'utente
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> ships = new ArrayList<>();
        
        // Esempio: inseriamo una nave da 2 posizioni
        Map<String, Object> nave = new HashMap<>();
        nave.put("name", "Cacciatorpediniere");
        List<Map<String, Integer>> pos = new ArrayList<>();
        pos.add(Map.of("x", 0, "y", 0));
        pos.add(Map.of("x", 0, "y", 1));
        nave.put("positions", pos);
        ships.add(nave);
        
        payload.put("ships", ships);
        
        miaGriglia.PosizionaNave(payload);
        comm.invia(new Messaggio(TipoMessaggio.PLACE_SHIPS_OK, payload));
    }

    private void eseguiAttacco() {
        System.out.print("Tuo turno! Inserisci coordinate attacco (x y): ");
        int x = scanner.nextInt();
        int y = scanner.nextInt();
        
        Map<String, Integer> payload = new HashMap<>();
        payload.put("x", x);
        payload.put("y", y);
        
        comm.invia(new Messaggio(TipoMessaggio.ATTACK, payload));
    }
}