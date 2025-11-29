package org.example;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class MainController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home(HttpServletRequest request) throws IOException {
        // Проверяем авторизацию через атрибуты, установленные фильтром
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            // Неавторизован - показываем страницу логина
            ClassPathResource loginFile = new ClassPathResource("templates/login.html");
            return StreamUtils.copyToString(loginFile.getInputStream(), StandardCharsets.UTF_8);
        }

        // Авторизован - показываем главную страницу
        ClassPathResource htmlFile = new ClassPathResource("templates/index.html");
        return StreamUtils.copyToString(htmlFile.getInputStream(), StandardCharsets.UTF_8);
    }

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String loginPage() throws IOException {
        ClassPathResource loginFile = new ClassPathResource("templates/login.html");
        return StreamUtils.copyToString(loginFile.getInputStream(), StandardCharsets.UTF_8);
    }
}