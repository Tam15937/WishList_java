package org.example.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketMessageController {

    @MessageMapping("/wishlist/update")
    @SendTo("/topic/wishlist")
    public String handleWishlistUpdate(String message) {
        // Тут можно обработать сообщение, например, сохранить обновление
        return message; // Ответ будет отправлен подписчикам /topic/wishlist
    }
}
