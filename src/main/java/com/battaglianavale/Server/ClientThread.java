package com.battaglianavale.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class ClientThread extends Thread {
    
    Socket socket;
    Gioco gioco;
    int playerId;
    Gson gson = new Gson();

    public ClientThread(Socket socket, Gioco gioco) {
        this.socket = socket;
        this.gioco = gioco;
    }
    public void run() {
        try{
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
            String richiesta;
            //legge le richieste dal client che manda al server
            while ((richiesta = reader.readLine()) != null) {
                Comunicazione comunicazione = gson.fromJson(richiesta,Comunicazione.class);
                gestoreMessaggi(comunicazione, writer);
            }
                
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    public void gestoreMessaggi(Comunicazione comunicazione, BufferedWriter writer) throws Exception {
        // se la comunicazione di tipo join crea la stringa e aggiunge un giocatore però se il gioco è pienio mandaa messaggio di errore
        
        switch (comunicazione.getType()) {
            case JOIN:
                String nomeGiocatore;
               
                //creazione mappa per ricavare il messaggio tramite payload

                Map<String,String> joinPayload = new HashMap<>();
                joinPayload = (Map<String,String>) comunicazione.getPayload();
                nomeGiocatore = joinPayload.get("playerName");


                this.playerId = gioco.AddGiocatore(nomeGiocatore);
                

                if (this.playerId == -1) {
                    //invia messaggio di errore
                    //usiamo le mappe perchè dobbiamo mettere nel payload dei datai

                    writer.write(gson.toJson(new Comunicazione(TipoMessaggio.ERROR, Map.of("message", "Numero massimo di giocatori raggiunto, Partita piena"))));
                    writer.newLine();
                    writer.flush();

                } else {
                    writer.write(gson.toJson(new Comunicazione(TipoMessaggio.JOIN_OK, Map.of("playerId", Integer.toString(this.playerId)))));
                    writer.newLine();
                    writer.flush();
                    //invia messaggio di JOIN_OK
                }
                break;
            case ATTACK:
                if(!gioco.turno(this.playerId)){
                    writer.write(gson.toJson(new Comunicazione(TipoMessaggio.ERROR, Map.of("message", "Non è il tuo turno"))));
                    writer.newLine();
                    writer.flush();
                }else{
                    Map<String,String> attackPayload = (Map<String,String>) comunicazione.getPayload();
                    int x = Integer.parseInt(attackPayload.get("x"));
                    int y = Integer.parseInt(attackPayload.get("y"));
                    // esegue l'atacco
                    String risultatoAttacco = gioco.getTabellaAvversario(this.playerId).Attacco(x, y);

                    Map<String,String> attackResultPayload = new HashMap<>();
                    attackResultPayload.put("x", Integer.toString(x));
                    attackResultPayload.put("y", Integer.toString(y));
                    attackResultPayload.put("result", risultatoAttacco);
                    writer.write(gson.toJson(new Comunicazione(TipoMessaggio.ATTACK_RESULT, attackResultPayload)));
                    writer.newLine();
                    writer.flush();
                    //cambia turno
                    gioco.cambiaTurno();
                }
                break;
            case PLACE_SHIPS:
                Map<String,Object> shipsPayload = (Map<String,Object>) comunicazione.getPayload();
                try{
                    gioco.posizionaNavi(this.playerId, shipsPayload);
                    writer.write(gson.toJson(new Comunicazione(TipoMessaggio.PLACE_SHIPS_OK, null)));
                    writer.newLine();
                    writer.flush();
                }catch(IllegalArgumentException e){
                    writer.write(gson.toJson(new Comunicazione(TipoMessaggio.ERROR, Map.of("message", e.getMessage()))));
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
