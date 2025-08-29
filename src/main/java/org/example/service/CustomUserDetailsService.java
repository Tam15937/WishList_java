package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.CustomUserDetails;
import org.example.model.UserModel;
import org.example.repository.UserRepository;
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

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // По username для стандартной аутентификации
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserModel user = userRepository.findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUserDetails(user);
    }

    // По токену для фильтра
    public UserDetails loadUserByToken(String token) {
        UserModel user = userRepository.findByAuthToken(token)
                .orElse(null);
        if (user == null) return null;
        return new CustomUserDetails(user);
    }

    // Генерация и сохранение токена при логине
    public String generateAndSaveToken(UserModel user) {
        String token = UUID.randomUUID().toString();
        user.setAuthToken(token);
        userRepository.save(user);
        return token;
    }

    public void logout(String token) {
        Optional<UserModel> userOpt = userRepository.findByAuthToken(token);
        userOpt.ifPresent(user -> {
            user.setAuthToken(null);
            userRepository.save(user);
        });
    }
}
