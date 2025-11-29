package org.example.security.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String authToken;
    private Long userId;
    private String username;
    private String message;

    // Конструкторы
    public AuthResponse() {
    }

    public AuthResponse(String authToken, Long userId, String username, String message) {
        this.authToken = authToken;
        this.userId = userId;
        this.username = username;
        this.message = message;
    }
}