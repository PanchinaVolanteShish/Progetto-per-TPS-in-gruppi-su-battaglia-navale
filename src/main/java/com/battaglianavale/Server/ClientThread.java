package com.battaglianavale.Server;

import java.io.BufferedWriter;
import java.net.Socket;
import java.nio.Buffer;
import java.util.Map;

public class ClientThread extends Thread {
    
    Socket socket;
    Gioco gioco;
    String nomeGiocatore;
    Gson gson = new Gson();

    public Thread(Socket socket, Gioco gioco) {
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
    public void gestoreMessaggi(Comunicazione comunicazione, BufferedWriter writer) {
        // se la comunicazione di tipo join crea la stringa e aggiunge un giocatore però se il gioco è pienio mandaa messaggio di errore
        
        switch (comunicazione.getType()) {
            case JOIN:
                String nomeGiocatore;
               
                //creazione mappa per ricavare il messaggio tramite payload

                Map<String,String> payload = new HashMap<>();
                payload= comunicazione.payload;
                nomeGiocatore = payload.get("nomeGiocatore");


                int playerId = gioco.AddGiocatore(nomeGiocatore);
                

                if (playerId == -1) {
                    //invia messaggio di errore
                    //usiamo le mappe perchè dobbiamo mettere nel payload dei datai

                    writer.write(gson.toJson(new Comunicazione(TipoMessaggio.ERROR, Map.of("message", "Numero massimo di giocatori raggiunto, Partita piena"))));
                    

                } else {
                    writer.write(gson.toJson(new Comunicazione(TipoMessaggio.JOIN_OK, Map.of("playerId", Integer.toString(playerId)))));    
                    //invia messaggio di JOIN_OK
                }
                break;
            case ATTACK:
                if(!gioco.turno(playerId)){
                    writer.write(gson.toJson(new Comunicazione(TipoMessaggio.ERROR, Map.of("message", "Non è il tuo turno"))));
                }else{
                    Map<String,String> payload= new HashMap<>();
                    payload= comunicazione.payload;
                    int x= Integer.parseInt(payload.get("x"));
                    int y= Integer.parseInt(payload.get("y"));
                    // esegue l'atacco
                    String risultatoAttacco= gioco.getTabellaAvversario(playerId).Attacco(x, y);

                    Map<String,String> attackResultPayload= new HashMap<>();
                    attackResultPayload.put("x", Integer.toString(x));
                    attackResultPayload.put("y", Integer.toString(y));
                    attackResultPayload.put("result", risultatoAttacco);
                    writer.write(gson.toJson(new Comunicazione(TipoMessaggio.ATTACK_RESULT, attackResultPayload)));
                    //cambia turno
                    gioco.cambiaTurno();
                }
                case PLACE_SHIPS:
                    Map<String,String> payload= new HashMap<>();
                    payload= comunicazione.payload;
                    try{
                        gioco.posizionaNavi(playerId, payload);
                        writer.write(gson.toJson(new Comunicazione(TipoMessaggio.PLACE_SHIPS_OK)));
                    }catch(IllegalArgumentException e){
                        writer.write(gson.toJson(new Comunicazione(TipoMessaggio.ERROR, Map.of("message", e.getMessage()))));
                    }
                    case GAME_START:
                        
                break;
            
            //gestione altri tipi di messaggi
        }

    }
}
