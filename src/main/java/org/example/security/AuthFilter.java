package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.security.dto.TokenValidationResponse;
import org.example.security.service.AuthService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public AuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return path.equals("/login") ||
                path.startsWith("/auth/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.equals("/favicon.ico") ||
                path.equals("/login.html") ||
                (path.equals("/users") && "POST".equalsIgnoreCase(method));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;

        // 1. Проверяем Authorization header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        // 2. Если нет в header, проверяем cookie
        else if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("auth_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            if (isHtmlRequest(request)) {
                response.sendRedirect("/login");
                return;
            }
            sendUnauthorizedError(response, "Missing or invalid authorization");
            return;
        }

        TokenValidationResponse validation = authService.validateToken(token);

        if (!validation.isValid()) {
            if (isHtmlRequest(request)) {
                // Удаляем невалидную cookie
                Cookie invalidCookie = new Cookie("auth_token", "");
                invalidCookie.setMaxAge(0);
                invalidCookie.setPath("/");
                response.addCookie(invalidCookie);
                response.sendRedirect("/login");
                return;
            }
            sendUnauthorizedError(response, "Invalid or expired token");
            return;
        }

        request.setAttribute("userId", validation.getUserId());
        request.setAttribute("username", validation.getUsername());

        filterChain.doFilter(request, response);
    }

    private boolean isHtmlRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        return acceptHeader != null && acceptHeader.contains("text/html");
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}