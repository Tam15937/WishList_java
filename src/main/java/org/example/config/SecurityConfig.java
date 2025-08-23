package org.example.config;

import jakarta.servlet.http.Cookie;
import org.example.model.User;
import org.example.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Разрешить доступ к странице логина и статике без авторизации
                        .requestMatchers("/login", "/css/login.css", "/js/login.js", "/images/**").permitAll()
                        // Для всего остального нужна аутентификация
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")           // GET страница логина
                        .loginProcessingUrl("/login")  // URL для POST с данными формы
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/perform_logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .deleteCookies("JSESSIONID", "username", "user_id")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance()); // или BCryptPasswordEncoder
        return authProvider;
    }
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            User user = (User) authentication.getPrincipal();
            Cookie cookieUsername = new Cookie("username", user.getUsername());
            Cookie cookieUserId = new Cookie("user_id", user.getId());
            cookieUsername.setPath("/");
            cookieUserId.setPath("/");
            response.addCookie(cookieUsername);
            response.addCookie(cookieUserId);
            response.sendRedirect("/");
        };
    }
}
