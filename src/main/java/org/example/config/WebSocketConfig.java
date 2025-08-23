package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Регистрируем конечную точку для WebSocket соединений с поддержкой SockJS fallback
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Включаем простой брокер сообщений с префиксами для рассылки сообщений
        registry.enableSimpleBroker("/topic", "/queue");
        // Префикс для сообщений от клиента, которые будут направлены контроллерам @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");
    }
}
