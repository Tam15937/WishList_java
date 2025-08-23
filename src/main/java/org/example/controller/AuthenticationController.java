package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthenticationController {

    @GetMapping("/login")
    public String loginPage() {
        // Возвращаем имя файла login.html из ресурсов (без расширения и пути)
        return "login";
    }
}
