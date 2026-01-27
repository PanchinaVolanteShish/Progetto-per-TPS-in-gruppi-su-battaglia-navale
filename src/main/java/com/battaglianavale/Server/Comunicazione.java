package com.battaglianavale.Server;

public class Comunicazione {

    TipoMessaggio type;
    Object payload;

    public Comunicazione(TipoMessaggio type, Object payload) {
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
