package org.example.security.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenValidationResponse {
    private Long userId;
    private String username;
    private boolean isValid;

    // Конструкторы
    public TokenValidationResponse() {
    }

    public TokenValidationResponse(Long userId, String username, boolean isValid) {
        this.userId = userId;
        this.username = username;
        this.isValid = isValid;
    }
}