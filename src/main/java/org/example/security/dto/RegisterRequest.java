package org.example.security.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String name;
    private String password;
    private String confirmPassword;

    // Конструкторы
    public RegisterRequest() {
    }

    public RegisterRequest(String name, String password, String confirmPassword) {
        this.name = name;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
}