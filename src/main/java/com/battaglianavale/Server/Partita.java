package com.battaglianavale.Server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class Partita { 
        int numeroGiocatori = 0; // Numero di giocatori previsto
        List<String> nomeGiocatori = new ArrayList<>();
        int turnoCorrente = 1; // Indica di chi è il turno (1 o 2)
        Griglia grigliaGiocatore1 = new Griglia();
        Griglia grigliaGiocatore2 = new Griglia();
        
        // Riferimenti ai writer dei client per comunicazione bidirezionale
        private Map<Integer, BufferedWriter> clientWriters = new HashMap<>();
        private Gson gson = new Gson();
        
        // Registra il writer di un client
        public synchronized void registraClient(int playerId, BufferedWriter writer) {
            clientWriters.put(playerId, writer);
        }

        public synchronized int AddGiocatore(String nomeGiocatore) {
            //controllo numero di giocatori che sia 2

            if(numeroGiocatori >= 2) {
                return -1; // Numero massimo di giocatori raggiunto
            }
            nomeGiocatori.add(nomeGiocatore);
            numeroGiocatori++;
            return numeroGiocatori;
        }

        public String getNomeGiocatore(int playerId) {
            if (playerId >= 1 && playerId <= nomeGiocatori.size()) {
                return nomeGiocatori.get(playerId - 1);
            }
            return null;
        }

        public String getAvversario(int playerId) {
            //restituisce il nome dell'avversario
            if (playerId == 1) {
                return nomeGiocatori.size() > 1 ? nomeGiocatori.get(1) : null;
            } else {
                return nomeGiocatori.size() > 0 ? nomeGiocatori.get(0) : null;
            }
        }

        public int getIdAvversario(int playerId) {
            return playerId == 1 ? 2 : 1;
        }

        //controlla se è il turno del giocatore
        public boolean turno(int playerId) {
           return turnoCorrente == playerId;
        }

        //cambia il turno
        public void cambiaTurno() {
            turnoCorrente = (turnoCorrente == 1) ? 2 : 1;
        }

        public int getTurnoCorrente() {
            return turnoCorrente;
        }

        public Griglia getGrigliaAvversario(int playerId) {
            //restituisce la griglia dell'avversario
            if (playerId == 1) {
                return grigliaGiocatore2;
            } else {
                return grigliaGiocatore1;
            }
        }

        public Griglia getGriglia(int playerId) {
            if (playerId == 1) {
                return grigliaGiocatore1;
            } else {
                return grigliaGiocatore2;
            }
        }

        public void posizionaNavi(int playerId, Map<String,Object> payload) {
            //posiziona le navi nella griglia del giocatore
            Griglia griglia = getGriglia(playerId);
            griglia.PosizionaNave(payload);
        }

        // Verifica se entrambi i giocatori hanno posizionato le navi
        public boolean entrambiPronti() {
            return grigliaGiocatore1.naviPosizionate() && grigliaGiocatore2.naviPosizionate();
        }

        // Invia messaggio GAME_START a entrambi i giocatori
        public void inviaGameStart() throws IOException {
            for (Map.Entry<Integer, BufferedWriter> entry : clientWriters.entrySet()) {
                int playerId = entry.getKey();
                BufferedWriter writer = entry.getValue();
                boolean yourTurn = (playerId == turnoCorrente);
                
                Map<String, Object> payload = new HashMap<>();
                payload.put("yourTurn", yourTurn);
                
                writer.write(gson.toJson(new Messaggio(TipoMessaggio.GAME_START, payload)));
                writer.newLine();
                writer.flush();
            }
        }

        // Invia INCOMING_ATTACK al difensore
        public void inviaIncomingAttack(int difensoreId, int x, int y, String result) throws IOException {
            BufferedWriter writer = clientWriters.get(difensoreId);
            if (writer != null) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("x", x);
                payload.put("y", y);
                payload.put("result", result);
                
                writer.write(gson.toJson(new Messaggio(TipoMessaggio.INCOMING_ATTACK, payload)));
                writer.newLine();
                writer.flush();
            }
        }

        // Invia TURN_CHANGE a entrambi i giocatori
        public void inviaTurnChange() throws IOException {
            for (Map.Entry<Integer, BufferedWriter> entry : clientWriters.entrySet()) {
                int playerId = entry.getKey();
                BufferedWriter writer = entry.getValue();
                boolean yourTurn = (playerId == turnoCorrente);
                
                Map<String, Object> payload = new HashMap<>();
                payload.put("yourTurn", yourTurn);
                
                writer.write(gson.toJson(new Messaggio(TipoMessaggio.TURN_CHANGE, payload)));
                writer.newLine();
                writer.flush();
            }
        }

        // Invia GAME_OVER a entrambi i giocatori
        public void inviaGameOver(String winner) throws IOException {
            for (BufferedWriter writer : clientWriters.values()) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("winner", winner);
                
                writer.write(gson.toJson(new Messaggio(TipoMessaggio.GAME_OVER, payload)));
                writer.newLine();
                writer.flush();
            }
        }

        // Verifica se il gioco è finito (tutte le navi di un giocatore affondate)
        public boolean isGameOver() {
            return grigliaGiocatore1.tutteNaviAffondate() || grigliaGiocatore2.tutteNaviAffondate();
        }

        // Restituisce il nome del vincitore
        public String getVincitore() {
            if (grigliaGiocatore1.tutteNaviAffondate()) {
                return getNomeGiocatore(2); // Giocatore 2 vince
            } else if (grigliaGiocatore2.tutteNaviAffondate()) {
                return getNomeGiocatore(1); // Giocatore 1 vince
            }
            return null;
        }
}
