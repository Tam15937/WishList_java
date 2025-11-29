package org.example;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.example.security.dto.TokenValidationResponse;
import org.example.security.service.AuthService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class MainController {
    private final AuthService authService;

    public MainController(AuthService authService) {
        this.authService = authService;
    }
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home(HttpServletRequest request) throws IOException {
        // Проверяем авторизацию через cookie напрямую
        String token = extractTokenFromCookies(request);

        if (token != null) {
            // Валидируем токен через authService
            TokenValidationResponse validation = authService.validateToken(token);
            if (validation.isValid()) {
                // Авторизован - показываем главную страницу
                ClassPathResource htmlFile = new ClassPathResource("templates/index.html");
                return StreamUtils.copyToString(htmlFile.getInputStream(), StandardCharsets.UTF_8);
            }
        }

        // Неавторизован - показываем страницу логина
        ClassPathResource loginFile = new ClassPathResource("templates/login.html");
        return StreamUtils.copyToString(loginFile.getInputStream(), StandardCharsets.UTF_8);
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String loginPage() throws IOException {
        ClassPathResource loginFile = new ClassPathResource("templates/login.html");
        return StreamUtils.copyToString(loginFile.getInputStream(), StandardCharsets.UTF_8);
    }
}