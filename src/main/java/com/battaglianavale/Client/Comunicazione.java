package com.battaglianavale.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import com.google.gson.Gson;
import com.battaglianavale.Server.Messaggio;
import com.battaglianavale.Server.TipoMessaggio;

public class Comunicazione {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;
    private Gioco gioco;
    private boolean connesso;
    
    public Comunicazione(String host, int porta, Gioco gioco) throws IOException {
        this.socket = new Socket(host, porta);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.gson = new Gson();
        this.gioco = gioco;
        this.connesso = true;
        
        // Avvia thread per ascoltare messaggi dal server
        new Thread(this::ascoltaMessaggi).start();
    }
    
    private void ascoltaMessaggi() {
        try {
            String linea;
            while (connesso && (linea = in.readLine()) != null) {
                Messaggio messaggio = gson.fromJson(linea, Messaggio.class);
                gestisciMessaggio(messaggio);
            }
        } catch (IOException e) {
            if (connesso) {
                System.err.println("Errore di connessione: " + e.getMessage());
                gioco.mostraErrore("Connessione al server persa");
            }
        } finally {
            chiudi();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void gestisciMessaggio(Messaggio messaggio) {
        Map<String, Object> payload = null;
        if (messaggio.getPayload() != null) {
            payload = (Map<String, Object>) messaggio.getPayload();
        }
        
        switch (messaggio.getType()) {
            case JOIN_OK:
                int playerId = ((Number) payload.get("playerId")).intValue();
                gioco.setPlayerId(playerId);
                gioco.posizionaNaviInterattivo();
                break;
                
            case PLACE_SHIPS_OK:
                gioco.setNaviPosizionate(true);
                System.out.println("Navi posizionate! In attesa dell'avversario...");
                break;
                
            case GAME_START:
                boolean tuoTurno = (boolean) payload.get("yourTurn");
                gioco.setMioTurno(tuoTurno);
                System.out.println("\n=== PARTITA INIZIATA ===");
                break;
                
            case ATTACK_RESULT:
                int x = ((Number) payload.get("x")).intValue();
                int y = ((Number) payload.get("y")).intValue();
                String risultato = (String) payload.get("result");
                gioco.aggiornaRisultatoAttacco(x, y, risultato, true);
                
                // Se non è SUNK, potrebbe essere il turno dell'avversario
                if (risultato.equals("MISS") || risultato.equals("ALREADY_ATTACKED")) {
                    gioco.setMioTurno(false);
                }
                break;
                
            case INCOMING_ATTACK:
                int xAtt = ((Number) payload.get("x")).intValue();
                int yAtt = ((Number) payload.get("y")).intValue();
                String risAtt = (String) payload.get("result");
                gioco.aggiornaRisultatoAttacco(xAtt, yAtt, risAtt, false);
                break;
                
            case TURN_CHANGE:
                boolean turno = (boolean) payload.get("yourTurn");
                gioco.setMioTurno(turno);
                break;
                
            case GAME_OVER:
                String vincitore = (String) payload.get("winner");
                gioco.finePartita(vincitore);
                break;
                
            case ERROR:
                String errore = (String) payload.get("message");
                gioco.mostraErrore(errore);
                break;
                
            default:
                System.err.println("Messaggio sconosciuto: " + messaggio.getType());
        }
    }
    
    // Metodi per inviare messaggi al server
    public void inviaJoin(String nomeGiocatore) {
        Map<String, Object> payload = Map.of("playerName", nomeGiocatore);
        inviaMessaggio(TipoMessaggio.JOIN, payload);
    }
    
    public void inviaPosizionamentoNavi(Map<String, Object> navi) {
        inviaMessaggio(TipoMessaggio.PLACE_SHIPS, navi);
    }
    
    public void inviaAttacco(int x, int y) {
        Map<String, Object> payload = Map.of("x", x, "y", y);
        inviaMessaggio(TipoMessaggio.ATTACK, payload);
    }
    
    private void inviaMessaggio(TipoMessaggio tipo, Object payload) {
        Messaggio messaggio = new Messaggio(tipo, payload);
        out.println(gson.toJson(messaggio));
    }
    
    public void chiudi() {
        connesso = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            // Ignora errori in chiusura
        }
    }
    
    public boolean isConnesso() {
        return connesso;
    }
}