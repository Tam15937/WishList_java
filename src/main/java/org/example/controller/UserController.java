package org.example.controller;

import org.example.model.UserModel;
import org.example.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) { this.userRepository = userRepository; }

    @GetMapping("/find")
    public UserModel findUser(@RequestParam String name) {
        Optional<UserModel> user = userRepository.findByName(name);
        return user.orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserModel createUser(@Valid @RequestBody UserModel user) {
        return userRepository.save(user);
    }

    @PutMapping("/{id}/token")
    public UserModel updateToken(@PathVariable Long id, @RequestParam String authToken) {
        return userRepository.findById(id).map(user -> {
            user.setAuthToken(authToken);
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }
}

