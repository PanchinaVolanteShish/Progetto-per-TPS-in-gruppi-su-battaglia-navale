package com.battaglianavale.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Griglia {
    private char[][] griglia = new char[10][10];
    private Map<String, List<int[]>> naviPosizionate = new HashMap<>();
    private Set<String> naviAffondate = new HashSet<>();
    private String ultimaNaveAffondata = null;

    public Griglia(){
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                griglia[i][j] = '~'; // Inizializza tutte le celle con acqua
            }
        }
    }

    public String Attacco(int x, int y){
        if (griglia[x][y] == '~') {
            griglia[x][y] = 'O'; // Segna il colpo mancato
            return "MISS";
        } else if (griglia[x][y] == 'B') {
            griglia[x][y] = 'X'; // Segna la nave colpita
            
            // Verifica se una nave è affondata
            String naveColpita = trovaNaveInPosizione(x, y);
            if (naveColpita != null && isNaveAffondata(naveColpita)) {
                naviAffondate.add(naveColpita);
                ultimaNaveAffondata = naveColpita;
                return "SUNK";
            }
            return "HIT";
        } else {
            return "ALREADY_ATTACKED";
        }
    }

    // Trova quale nave occupa la posizione specificata
    private String trovaNaveInPosizione(int x, int y) {
        for (Map.Entry<String, List<int[]>> entry : naviPosizionate.entrySet()) {
            for (int[] coord : entry.getValue()) {
                if (coord[0] == x && coord[1] == y) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    // Verifica se tutte le posizioni di una nave sono state colpite
    private boolean isNaveAffondata(String nomeNave) {
        List<int[]> posizioni = naviPosizionate.get(nomeNave);
        if (posizioni == null) return false;
        
        for (int[] coord : posizioni) {
            if (griglia[coord[0]][coord[1]] != 'X') {
                return false;
            }
        }
        return true;
    }

    // Restituisce il nome dell'ultima nave affondata
    public String getUltimaNaveAffondata() {
        return ultimaNaveAffondata;
    }

    // Verifica se tutte le navi sono affondate
    public boolean tutteNaviAffondate() {
        return naviAffondate.size() == naviPosizionate.size() && naviPosizionate.size() > 0;
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

                if (griglia[x][y] != '~') {
                    throw new IllegalArgumentException("Posizione non valida per la nave: " + nome);
                } else {
                    griglia[x][y] = 'B'; // Posiziona la nave
                    coords.add(new int[]{x, y});
                }
            }
            naviPosizionate.put(nome, coords);
        }
    }

    // Verifica se le navi sono state posizionate
    public boolean naviPosizionate() {
        return naviPosizionate.size() > 0;
    }
}
