package org.example.controller;

import org.example.model.ListModel;
import org.example.repository.ListRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lists")
public class ListController {

    private final ListRepository listRepository;

    public ListController(ListRepository listRepository) { this.listRepository = listRepository; }

    @GetMapping
    public List<ListModel> getAllLists() {
        return listRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<ListModel> getListsByUser(@PathVariable Long userId) {
        return listRepository.findByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListModel createList(@Valid @RequestBody ListModel listModel) {
        return listRepository.save(listModel);
    }
}
