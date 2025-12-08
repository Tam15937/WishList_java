package org.example.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @NotBlank
    private String passwordHash;

    @Column(unique = true)
    private String nameHash; // Хеш имени для аутентификации

    @Column(unique = true)
    private String authToken;

    private LocalDateTime tokenExpiry;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt; // ДОБАВЬТЕ ЭТО ПОЛЕ
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastLogin = LocalDateTime.now();
    }
}