# Protocollo di Gioco - Battaglia Navale

Il gioco deve rispettare il seguente protocollo di comunicazione basato su messaggi JSON.

## 1. Concetto base del protocollo
Ogni messaggio JSON deve avere almeno i seguenti campi:

```json
{
  "type": "NOME_MESSAGGIO",
  "payload": { ... }
}
```

*   **type**: identifica l’azione.
*   **payload**: contiene i dati specifici.

Questo rende più semplice il parsing lato server/client.

---

## 2. Fase di connessione

### 2.1 Connessione / Join partita

**Client → Server**
```json
{
  "type": "JOIN",
  "payload": {
    "playerName": "Marco"
  }
}
```

**Server → Client (Successo)**
```json
{
  "type": "JOIN_OK",
  "payload": {
    "playerId": 1
  }
}
```

**Server → Client (Errore)**
```json
{
  "type": "ERROR",
  "payload": {
    "message": "Partita già piena"
  }
}
```

---

## 3. Posizionamento delle navi
Strategia:
*   Il client manda tutte le navi.
*   Il server valida e salva.

### 3.1 Invio flotta

**Client → Server**
```json
{
  "type": "PLACE_SHIPS",
  "payload": {
    "ships": [
      {
        "name": "Destroyer",
        "size": 2,
        "positions": [
          { "x": 3, "y": 5 },
          { "x": 4, "y": 5 }
        ]
      },
      {
        "name": "Submarine",
        "size": 3,
        "positions": [
          { "x": 1, "y": 2 },
          { "x": 1, "y": 4 }
        ]
      }
    ]
  }
}
```

**Server → Client (Conferma)**
```json
{
  "type": "PLACE_SHIPS_OK"
}
```

Quando entrambi i giocatori hanno posizionato le navi:

**Server → Client (Inizio Gioco)**
```json
{
  "type": "GAME_START",
  "payload": {
    "yourTurn": true
  }
}
```

---

## 4. Turno di gioco

### 4.1 Attacco

**Client → Server**
```json
{
  "type": "ATTACK",
  "payload": {
    "x": 4,
    "y": 7
  }
}
```

### 4.2 Risultato dell’attacco

**Server → Client (Attaccante)**
```json
{
  "type": "ATTACK_RESULT",
  "payload": {
    "x": 4,
    "y": 7,
    "result": "HIT"
  }
}
```

Possibili valori di `result`:
*   `"MISS"` (Mancato)
*   `"HIT"` (Colpito)
*   `"SUNK"` (Affondato)

Se una nave viene affondata, il payload include il nome della nave:
```json
{
  "type": "ATTACK_RESULT",
  "payload": {
    "x": 4,
    "y": 7,
    "result": "SUNK",
    "ship": "Submarine"
  }
}
```

### 4.3 Notifica al difensore

**Server → Client (Difensore)**
```json
{
  "type": "INCOMING_ATTACK",
  "payload": {
    "x": 4,
    "y": 7,
    "result": "HIT"
  }
}
```

---

## 5. Cambio turno

**Server → Client**
```json
{
  "type": "TURN_CHANGE",
  "payload": {
    "yourTurn": false
  }
}
```

---

## 6. Fine partita

**Server → Client**
```json
{
  "type": "GAME_OVER",
  "payload": {
    "winner": "Marco"
  }
}
```

---

## 7. Messaggi di errore
Utile per la validazione lato server.

**Server → Client**
```json
{
  "type": "ERROR",
  "payload": {
    "message": "Mossa non valida",
    "details": "Coordinate già utilizzate"
  }
}
```

---

## 8. Riassunto dei tipi di messaggio

| Tipo | Direzione | Scopo |
| :--- | :--- | :--- |
| **JOIN** | Client → Server | Entrata in partita |
| **JOIN_OK** | Server → Client | Conferma entrata |
| **PLACE_SHIPS** | Client → Server | Posizionamento navi |
| **PLACE_SHIPS_OK** | Server → Client | Conferma posizionamento |
| **GAME_START** | Server → Client | Inizio gioco |
| **ATTACK** | Client → Server | Esecuzione attacco |
| **ATTACK_RESULT** | Server → Client | Esito attacco (per chi attacca) |
| **INCOMING_ATTACK** | Server → Client | Notifica attacco subito (per chi difende) |
| **TURN_CHANGE** | Server → Client | Cambio turno |
| **GAME_OVER** | Server → Client | Fine partita |
| **ERROR** | Server → Client | Segnalazione errore |
