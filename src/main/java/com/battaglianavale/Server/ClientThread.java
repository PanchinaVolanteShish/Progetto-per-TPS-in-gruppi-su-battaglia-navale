package com.battaglianavale.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class ClientThread extends Thread {
    
    Socket socket;
    Partita partita;
    int playerId;
    BufferedWriter writer;
    Gson gson = new Gson();

    public ClientThread(Socket socket, Partita partita) {
        this.socket = socket;
        this.partita = partita;
    }
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            writer = bw;
            String richiesta;
            //legge le richieste dal client che manda al server
            while ((richiesta = reader.readLine()) != null) {
                Messaggio messaggio = gson.fromJson(richiesta,Messaggio.class);
                gestoreMessaggi(messaggio, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressWarnings("unchecked")
    public void gestoreMessaggi(Messaggio messaggio, BufferedWriter writer) throws Exception {
        // se la comunicazione di tipo join crea la stringa e aggiunge un giocatore però se il gioco è pienio mandaa messaggio di errore
        
        switch (messaggio.getType()) {
            case JOIN:
                String nomeGiocatore;
               
                //creazione mappa per ricavare il messaggio tramite payload

                Map<String,String> joinPayload = new HashMap<>();
                joinPayload = (Map<String,String>) messaggio.getPayload();
                nomeGiocatore = joinPayload.get("playerName");


                this.playerId = partita.AddGiocatore(nomeGiocatore);
                

                if (this.playerId == -1) {
                    //invia messaggio di errore
                    //usiamo le mappe perchè dobbiamo mettere nel payload dei datai

                    writer.write(gson.toJson(new Messaggio(TipoMessaggio.ERROR, Map.of("message", "Numero massimo di giocatori raggiunto, Partita piena"))));
                    writer.newLine();
                    writer.flush();

                } else {
                    // Registra il writer del client nella partita
                    partita.registraClient(this.playerId, writer);
                    
                    writer.write(gson.toJson(new Messaggio(TipoMessaggio.JOIN_OK, Map.of("playerId", this.playerId))));
                    writer.newLine();
                    writer.flush();
                }
                break;
            case ATTACK:
                if(!partita.turno(this.playerId)){
                    writer.write(gson.toJson(new Messaggio(TipoMessaggio.ERROR, Map.of("message", "Non è il tuo turno"))));
                    writer.newLine();
                    writer.flush();
                }else{
                    Map<String,Object> attackPayload = (Map<String,Object>) messaggio.getPayload();
                    int x = ((Number) attackPayload.get("x")).intValue();
                    int y = ((Number) attackPayload.get("y")).intValue();
                    
                    // Ottieni la griglia dell'avversario e esegui l'attacco
                    Griglia grigliaAvversario = partita.getGrigliaAvversario(this.playerId);
                    String risultatoAttacco = grigliaAvversario.Attacco(x, y);

                    // Prepara il payload per ATTACK_RESULT
                    Map<String,Object> attackResultPayload = new HashMap<>();
                    attackResultPayload.put("x", x);
                    attackResultPayload.put("y", y);
                    attackResultPayload.put("result", risultatoAttacco);
                    
                    // Se la nave è affondata, aggiungi il nome della nave
                    if (risultatoAttacco.equals("SUNK")) {
                        attackResultPayload.put("ship", grigliaAvversario.getUltimaNaveAffondata());
                    }
                    
                    // Invia ATTACK_RESULT all'attaccante
                    writer.write(gson.toJson(new Messaggio(TipoMessaggio.ATTACK_RESULT, attackResultPayload)));
                    writer.newLine();
                    writer.flush();
                    
                    // Invia INCOMING_ATTACK al difensore
                    int difensoreId = partita.getIdAvversario(this.playerId);
                    partita.inviaIncomingAttack(difensoreId, x, y, risultatoAttacco);
                    
                    // Verifica se il gioco è finito
                    if (partita.isGameOver()) {
                        String vincitore = partita.getVincitore();
                        partita.inviaGameOver(vincitore);
                    } else {
                        // Cambia turno e notifica entrambi i giocatori
                        partita.cambiaTurno();
                        partita.inviaTurnChange();
                    }
                }
                break;
            case PLACE_SHIPS:
                Map<String,Object> shipsPayload = (Map<String,Object>) messaggio.getPayload();
                try{
                    partita.posizionaNavi(this.playerId, shipsPayload);
                    writer.write(gson.toJson(new Messaggio(TipoMessaggio.PLACE_SHIPS_OK, null)));
                    writer.newLine();
                    writer.flush();
                    
                    // Verifica se entrambi i giocatori hanno posizionato le navi
                    if (partita.entrambiPronti()) {
                        partita.inviaGameStart();
                    }
                }catch(IllegalArgumentException e){
                    writer.write(gson.toJson(new Messaggio(TipoMessaggio.ERROR, Map.of("message", e.getMessage()))));
                    writer.newLine();
                    writer.flush();
                }
                break;
            default:
                break;
            
            //gestione altri tipi di messaggi
        }

    }
}
