package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File file = Paths.get("users.json").toFile(); // путь к JSON файлу с пользователями

    private Map<String, User> userMap = new HashMap<>();  // ключ - username

    public CustomUserDetailsService() {
        loadUsers();
    }

    private void loadUsers() {
        try {
            if (file.exists()) {
                List<User> users = objectMapper.readValue(file, new TypeReference<List<User>>() {});
                for (User user : users) {
                    userMap.put(user.getUsername(), user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, userMap.values());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMap.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        // Здесь по желанию добавьте роли/права
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }

    /**
     * Метод для аутентификации или создания пользователя с сохранением в JSON (по примеру вашего UserAuthService)
     */
    public User authenticateOrCreateUser(String username, String password) {
        User user = userMap.get(username);
        if (user == null) {
            // создать нового пользователя
            String id = UUID.randomUUID().toString();
            user = new User(id, username, password);
            userMap.put(username, user);
            saveUsers();
        } else {
            // проверка пароля
            if (!user.getPassword().equals(password)) {
                throw new UsernameNotFoundException("Неверный пароль");
            }
        }
        return user;
    }
}
