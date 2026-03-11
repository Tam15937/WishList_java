package org.example.webSocket.eventsListener;

import org.example.data.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WebSocketSessionListener {

    private final UserRepository userRepository;

    public WebSocketSessionListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener
    @Transactional
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                // На всякий случай подтверждаем активность при коннекте
                user.setActive(true);
                userRepository.save(user);
                System.out.println("=== USER CONNECTED ===");
                System.out.println("User: " + user.getName() + " (ID: " + userId + ") established WS connection");
            });
        }
    }

    @EventListener
    @Transactional
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                user.setActive(false);
                userRepository.save(user);
                System.out.println("=== USER DISCONNECTED ===");
                System.out.println("User: " + user.getName() + " is now INACTIVE");
            });
        }
    }
}