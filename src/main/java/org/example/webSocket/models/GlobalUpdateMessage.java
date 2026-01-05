package org.example.webSocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GlobalUpdateMessage {

    private String blockKey;
    private String payload;

    public GlobalUpdateMessage() {
    }

    public GlobalUpdateMessage(String blockKey, String payload) {
        this.blockKey = blockKey;
        this.payload = payload;
    }
}
