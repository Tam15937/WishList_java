package org.example.security.service;

import jakarta.transaction.Transactional;
import org.example.data.model.UserModel;
import org.example.data.repository.UserRepository;
import org.example.security.dto.AuthResponse;
import org.example.security.dto.LoginRequest;
import org.example.security.dto.RegisterRequest;
import org.example.security.dto.TokenValidationResponse;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        System.out.println("=== REGISTRATION START ===");
        System.out.println("Username: " + request.getName());

        if (!request.getName().matches("^[a-zA-Z0-9а-яА-Я]+$")) {
            throw new RuntimeException("Имя пользователя может содержать только кирилицу, латиницу, цифры");
        }

        // 1. Проверяем, что пароли совпадают
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Пароли не совпадают");
        }

        // 2. Проверяем длину пароля
        if (request.getPassword().length() < 6) {
            throw new RuntimeException("Минимальная длинна пароля 6 символов");
        }

        // 3. Проверяем длину имени пользователя
        if (request.getName().length() < 4) {
            throw new RuntimeException("Минимальная длинна имени пользователя 4 символа");
        }

        // 4. Хешируем имя для поиска существующего пользователя
        String nameHash = passwordService.hashUsername(request.getName());

        // 5. Проверяем существование пользователя по хешу имени
        boolean userExists = userRepository.existsByNameHash(nameHash);

        if (userExists) {
            throw new RuntimeException("Имя пользователя занято");
        }

        // 6. Хешируем пароль
        String passwordHash = passwordService.hashPassword(request.getPassword());

        // 7. Генерируем токен
        String authToken = generateToken();

        // 8. Создаем нового пользователя
        UserModel newUser = new UserModel();
        newUser.setName(request.getName());
        newUser.setNameHash(nameHash);
        newUser.setPasswordHash(passwordHash);
        newUser.setAuthToken(authToken);
        newUser.setTokenExpiry(LocalDateTime.now().plusHours(24));
        newUser.setLastLogin(LocalDateTime.now());
        newUser.setActive(true);

        // 9. Сохраняем пользователя
        userRepository.save(newUser);

        System.out.println("Registration SUCCESS for user: " + newUser.getName());
        return new AuthResponse(authToken, newUser.getId(), newUser.getName(), "Registration successful");
    }

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        System.out.println("=== AUTHENTICATION START ===");
        System.out.println("Username: " + request.getName());

        // 1. Хешируем имя для поиска
        String nameHash = passwordService.hashUsername(request.getName());
        System.out.println("Name hash: " + nameHash);

        // 2. Ищем пользователя по хешу имени
        Optional<UserModel> userOpt = userRepository.findByNameHash(nameHash);

        if (userOpt.isEmpty()) {
            System.out.println("User not found by name hash");
            throw new RuntimeException("Неверное имя пользователя или пароль");
        }

        UserModel user = userOpt.get();
        System.out.println("User found: " + user.getName());

        // 3. Проверяем пароль с помощью verifyPassword
        boolean passwordValid = passwordService.verifyPassword(request.getPassword(), user.getPasswordHash());
        System.out.println("Password valid: " + passwordValid);

        if (!passwordValid) {
            System.out.println("Password verification failed");
            throw new RuntimeException("Неверное имя пользователя или пароль");
        }

        // 4. Генерируем новый токен
        String newToken = generateToken();
        user.setAuthToken(newToken);
        user.setTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setLastLogin(LocalDateTime.now());

        userRepository.save(user);

        System.out.println("Authentication SUCCESS for user: " + user.getName());
        return new AuthResponse(newToken, user.getId(), user.getName(), "Login successful");
    }

    public TokenValidationResponse validateToken(String token) {
        if (token == null || token.isBlank()) {
            return new TokenValidationResponse(null, null, false);
        }

        UserModel user = userRepository.findByAuthToken(token)
                .orElse(null);

        if (user == null) {
            return new TokenValidationResponse(null, null, false);
        }
/*        System.out.println("=== TOKEN VALIDATION ===");
        System.out.println("Token: " + token);
        System.out.println("User found: " + (user != null));
        if (user != null) {
            System.out.println("Token expiry: " + user.getTokenExpiry());
            System.out.println("Is after now: " + user.getTokenExpiry().isAfter(LocalDateTime.now()));
            System.out.println("User active: " + user.getActive());
        }*/
        boolean isValid = user.getTokenExpiry() != null &&
                user.getTokenExpiry().isAfter(LocalDateTime.now()) &&
                Boolean.TRUE.equals(user.getActive());

        return new TokenValidationResponse(user.getId(), user.getName(), isValid);
    }

    public void logout(String token) {
        UserModel user = userRepository.findByAuthToken(token)
                .orElse(null);

        if (user != null) {
            user.setAuthToken(null);
            user.setTokenExpiry(null);
            userRepository.save(user);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}