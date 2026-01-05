package org.example.webSocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateMessage {

    private Long categoryId;
    private String blockKey;
    private String payload;

    public CategoryUpdateMessage() {
    }

    public CategoryUpdateMessage(Long categoryId, String blockKey, String payload) {
        this.categoryId = categoryId;
        this.blockKey = blockKey;
        this.payload = payload;
    }

}
