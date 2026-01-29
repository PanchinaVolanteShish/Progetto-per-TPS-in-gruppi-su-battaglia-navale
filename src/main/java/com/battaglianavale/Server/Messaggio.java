package com.battaglianavale.Server;

public class Messaggio {

    TipoMessaggio type;
    Object payload;

    public Messaggio(TipoMessaggio type, Object payload) {
        this.type = type;
        this.payload = payload;
    }
    public TipoMessaggio getType() {
        return type;
    }
    public Object getPayload() {
        return payload;
    }
    
}
