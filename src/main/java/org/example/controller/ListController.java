package org.example.controller;

import org.example.model.ListModel;
import org.example.model.UserModel;
import org.example.repository.ListRepository;
import jakarta.validation.Valid;
import org.example.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lists")
public class ListController {

    private final ListRepository listRepository;
    private final UserRepository userRepository;

    public ListController(ListRepository listRepository, UserRepository userRepository) {
        this.listRepository = listRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<ListModel> getAllLists() {
        return listRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<ListModel> getListsByUser(@PathVariable Long userId) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return listRepository.findByUser(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListModel createList(@Valid @RequestBody ListModel listModel) {
        // Валидация user (если нужно) и сохранение
        return listRepository.save(listModel);
    }
}
