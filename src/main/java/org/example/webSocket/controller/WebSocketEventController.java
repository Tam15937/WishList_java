package org.example.webSocket.controller;

import org.example.webSocket.models.CategoryUpdateMessage;
import org.example.webSocket.models.GlobalUpdateMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketEventController {

    @MessageMapping("/global.update")
    @SendTo("/topic/global")
    public GlobalUpdateMessage globalUpdate(@Payload GlobalUpdateMessage message) {
        /*System.out.println("=== ГЛОБАЛЬНОЕ СООБЩЕНИЕ ПРИЁМ ===");
        System.out.println("blockKey: " + message.getBlockKey());
        System.out.println("payload: " + message.getPayload());
        System.out.println("=====================================");*/
        return message;
    }

    @MessageMapping("/category.update.{categoryId}")
    @SendTo("/topic/category.{categoryId}")
    public CategoryUpdateMessage categoryUpdate(
            @DestinationVariable("categoryId") Long categoryId,
            @Payload CategoryUpdateMessage message
    ) {
        /*System.out.println("=== КАТЕГОРИЯ " + categoryId + " ===");
        System.out.println("blockKey: " + message.getBlockKey());
        System.out.println("payload: " + message.getPayload());
        System.out.println("================================");*/
        message.setCategoryId(categoryId);
        return message;
    }
}
