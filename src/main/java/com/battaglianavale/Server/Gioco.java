package com.battaglianavale.Server;

public class Gioco { 
        int numeroGiocatori = 0; // Numero di giocatori previsto
        List<String> nomeGiocatori= new ArrayList<>();
        Gson gson = new Gson();
        int turnoCorrente = 0; // Indica di chi è il turno
        Tabella tabellaGiocatore2=new Tabella();
        Tabella tabellaGiocatore1=new Tabella();
        

        public synchronized int AddGiocatore(String nomeGiocatore) {
            //controllo numero di giocatori che sia 2

            if(numeroGiocatori >= 2) {
                return -1; // Numero massimo di giocatori raggiunto
            }
            nomeGiocatori.add(nomeGiocatore);
            numeroGiocatori++;
            return numeroGiocatori;
        }

        public String getAvversario(int playerId) {
            //restituisce il nome dell'avversario
            if (playerId == 0) {
                return nomeGiocatori.get(1);
            } else {
                return nomeGiocatori.get(0);
            }
 

        }

        //controlla se è il turno del giocatore
        public boolean turno(int playerId) {
           if (turnoCorrente == playerId) {
                return true;
            } else {
                return false;
            }  

        }
        //cambia il turno
        public void cambiaTurno() {
            if (turnoCorrente == 0) {
                turnoCorrente = 1;
            } else {
                turnoCorrente = 0;
            }

        }
        public char getTabellaAvversario(int playerId) {
            //restituisce il contenuto della tabella dell'avversario
            if (playerId == 0) {
                return tabellaGiocatore2;
            } else {
                return tabellaGiocatore1;
                
            }
        }
        public void posizionaNavi(int playerId, Map<String,Object> payload) {
            //posiziona le navi nella tabella del giocatore
            Tabella tabella;
            if (playerId == 0) {
                tabella = tabellaGiocatore1;
            } else {
                tabella = tabellaGiocatore2;
            }
           
        }

}
