package com.battaglianavale.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Tabella {
    private char[][] tabella = new char[10][10];
    private Map<String, List<int[]>> naviPosizionate = new HashMap<>();
    private Set<String> naviAffondate = new HashSet<>();

    public Tabella(){
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                tabella[i][j] = '~'; // Inizializza tutte le celle con acqua
            }
        }
    }

    public String Attacco(int x, int y){
        if (tabella[x][y] == '~') {
            tabella[x][y] = 'O'; // Segna il colpo mancato
            return "MISS";
        } else if (tabella[x][y] == 'B') {
            tabella[x][y] = 'X'; // Segna la nave colpita
            return "HIT";
        } else {
            return "ALREADY_ATTACKED";
        }
    }

    public void PosizionaNave(Map<String,Object> payload){
        List<?> listaNavi = (List<?>) payload.get("ships");
        for (Object obj : listaNavi) {
            Map<?,?> nave = (Map<?,?>) obj;
            String nome = (String) nave.get("name");
            List<?> posizioni = (List<?>) nave.get("positions");
            List<int[]> coords = new ArrayList<>();
            
            for (Object obj2 : posizioni) {
                Map<?,?> pos = (Map<?,?>) obj2;
                int x = ((Number) pos.get("x")).intValue();
                int y = ((Number) pos.get("y")).intValue();

                if (tabella[x][y] != '~') {
                    throw new IllegalArgumentException("Posizione non valida per la nave: " + nome);
                } else {
                    tabella[x][y] = 'B'; // Posiziona la nave
                    coords.add(new int[]{x, y});
                }
            }
            naviPosizionate.put(nome, coords);
        }
    }
}