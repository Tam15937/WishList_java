package org.example.data.controller;

import org.example.data.model.UserModel;
import org.example.data.repository.UserRepository;
import jakarta.validation.Valid;
import org.example.security.service.PasswordService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserController(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    // создание пользователя с хешированием
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserModel createUser(@Valid @RequestBody UserModel user) {
        // Хешируем имя для проверки существования
        String nameHash = passwordService.hashUsername(user.getName());

        // Проверяем, не существует ли пользователь с таким хешем имени
        if (userRepository.existsByNameHash(nameHash)) {
            throw new RuntimeException("User with this name already exists");
        }

        // Хешируем пароль
        String hashedPassword = passwordService.hashPassword(user.getPasswordHash());
        user.setPasswordHash(hashedPassword);

        // Сохраняем хеш имени
        user.setNameHash(nameHash);

        return userRepository.save(user);
    }

    // получение пользователя по ID
    @GetMapping("/{id}")
    public UserModel getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // обновление пользователя по ID
    @PutMapping("/{id}")
    public UserModel updateUser(@PathVariable Long id, @Valid @RequestBody UserModel userDetails) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Если меняется имя, нужно обновить хеш
        if (userDetails.getName() != null && !userDetails.getName().equals(user.getName())) {
            String newNameHash = passwordService.hashUsername(userDetails.getName());

            // Проверяем, не занято ли новое имя
            if (userRepository.existsByNameHash(newNameHash)) {
                throw new RuntimeException("User with this name already exists");
            }

            user.setName(userDetails.getName());
            user.setNameHash(newNameHash);
        }

        // Если меняется пароль, хешируем его
        if (userDetails.getPasswordHash() != null) {
            String newPasswordHash = passwordService.hashPassword(userDetails.getPasswordHash());
            user.setPasswordHash(newPasswordHash);
        }

        return userRepository.save(user);
    }

    // удаление пользователя по ID
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
    /*
    TODO далее добавить обновление значение токена,
        который нужно получать после авторизации,
        так же искать пользователя по хешу от имени и пароля.
     */
}

