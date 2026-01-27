package com.battaglianavale.Server;

import java.util.List;
import java.util.Map;

public class Tabella {
    private char[][] tabella = new char[10][10];
    private Map<String, List<int[]>> naviPosizionate = new HashMap<>();
    private Set<String> naviAffondate= new HashSet<>();

    public Tabella(){
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                tabella[i][j] = '~'; // Inizializza tutte le celle con acqua
            }
        }
    }
    public String Attacco(int x,int y){
        if (tabella[x][y] == '~'){ {
            return "MISS";
        }else if (tabella[x][y]=='B') {
            tabella[x][y]='X'; // Segna la nave colpita
            return "HIT";
        }else{
            return "ALREADY ATTACKED";
            
        }
    }
}
public void PosizionaNave(Map<String,Object> payload){
    List<?> listanvi= (List<?>) payload.get("ships");
    for (Object obj : listanvi) {
        Map<?,?> nave = (Map<?,?>) obj;
        String nome = (String) nave.get("name");
        int size = (int) nave.get("size");
        //prendo la lista
        List<?> posizione = (List<?>) nave.get("position");
        for (Object obj2 : posizione) {
            Map<?,?> pos = (Map<?,?>) obj2;
            int x = (int) pos.get("x");
            int y = (int) pos.get("y");
            List<int[]> coords = new ArrayList<>();

            if (tabella[x][y]!='~') {
                throw new IllegalArgumentException("Posizione non valida per la nave: " + nome);
            } else{
                tabella[x][y]='B'; // Posiziona la nave
                //aggiunge cordinata
                coords.add(new int[]{x, y});

            }

                
            }
            naviPosizionate.put(nome, coords);
        }
        
       
    }

    
}