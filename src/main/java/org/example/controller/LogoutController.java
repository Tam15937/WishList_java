package org.example.controller;

import org.example.service.CustomUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class LogoutController {

    private final CustomUserDetailsService userDetailsService;

    public LogoutController(CustomUserDetailsService uds) {
        this.userDetailsService = uds;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid Authorization header");
        }

        String token = authHeader.substring(7);
        userDetailsService.logout(token);
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("Logged out successfully");
    }
}
