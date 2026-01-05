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

    @GetMapping(value = {"/", "/index"}, produces = MediaType.TEXT_HTML_VALUE)
    public String home(HttpServletRequest request) throws IOException {
        String token = extractTokenFromCookies(request);

        // Если пользователь авторизован
        if (token != null) {
            TokenValidationResponse validation = authService.validateToken(token);

            if (validation.isValid()) {
                // Определяем, мобильный ли клиент
                boolean isMobile = isMobileClient(request);

                // Подгружаем нужную HTML-страницу в зависимости от устройства
                ClassPathResource htmlFile = isMobile
                        ? new ClassPathResource("templates/mainpage_mobile.html")
                        : new ClassPathResource("templates/mainpage.html");

                return StreamUtils.copyToString(htmlFile.getInputStream(), StandardCharsets.UTF_8);
            }
        }

        // Если пользователь не авторизован, показываем страницу входа
        ClassPathResource loginFile = new ClassPathResource("templates/index.html");
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

    private boolean isMobileClient(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return false;

        // Простая проверка на мобильные устройства
        String ua = userAgent.toLowerCase();
        return ua.contains("android") ||
                ua.contains("iphone") ||
                ua.contains("ipad") ||
                ua.contains("ipod") ||
                ua.contains("windows phone") ||
                ua.contains("mobile");
    }
}