package org.example.data.controller;

import org.example.data.model.UserModel;
import org.example.data.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) { this.userRepository = userRepository; }

    // создание пользователя
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserModel createUser(@Valid @RequestBody UserModel user) {
        return userRepository.save(user);
    }

    // удаление пользователя
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

