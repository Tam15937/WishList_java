package org.example.security.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.example.security.dto.AuthResponse;
import org.example.security.dto.LoginRequest;
import org.example.security.dto.RegisterRequest;
import org.example.security.dto.TokenValidationResponse;
import org.example.security.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpServletResponse servletResponse) {
        try {
            AuthResponse response = authService.register(request);

            // Устанавливаем cookies так же, как при логине
            jakarta.servlet.http.Cookie authCookie = new jakarta.servlet.http.Cookie("auth_token", response.getAuthToken());
            authCookie.setHttpOnly(true);
            authCookie.setMaxAge(24 * 60 * 60); // 24 часа
            authCookie.setPath("/");
            authCookie.setSecure(false);
            servletResponse.addCookie(authCookie);

            Cookie userIdCookie = new Cookie("user_id", String.valueOf(response.getUserId()));
            userIdCookie.setPath("/");
            userIdCookie.setMaxAge(24 * 60 * 60);
            servletResponse.addCookie(userIdCookie);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse servletResponse) {
        try {
            AuthResponse response = authService.authenticate(request);

            // Устанавливаем cookie
            jakarta.servlet.http.Cookie authCookie = new jakarta.servlet.http.Cookie("auth_token", response.getAuthToken());
            authCookie.setHttpOnly(true);
            authCookie.setMaxAge(24 * 60 * 60); // 24 часа
            authCookie.setPath("/");
            authCookie.setSecure(false); // Для localhost - false, в production - true
            servletResponse.addCookie(authCookie);
            Cookie userIdCookie = new Cookie("user_id", String.valueOf(response.getUserId()));
            userIdCookie.setPath("/");
            userIdCookie.setMaxAge(24 * 60 * 60);
            servletResponse.addCookie(userIdCookie);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = "Authorization", required = false) String token,
                       HttpServletResponse servletResponse) {
        String cleanToken = null;

        if (token != null && token.startsWith("Bearer ")) {
            cleanToken = token.substring(7);
        }

        authService.logout(cleanToken);

        // Удаляем cookie
        jakarta.servlet.http.Cookie invalidCookie = new jakarta.servlet.http.Cookie("auth_token", "");
        invalidCookie.setMaxAge(0);
        invalidCookie.setPath("/");
        servletResponse.addCookie(invalidCookie);
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Убираем "Bearer "если есть
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return ResponseEntity.ok(authService.validateToken(cleanToken));
    }

}