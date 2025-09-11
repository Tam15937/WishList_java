package org.example.data.controller;

import org.example.data.model.ListItemModel;
import org.example.data.model.ListModel;
import org.example.data.model.UserModel;
import org.example.data.repository.ListItemRepository;
import org.example.data.repository.ListRepository;
import jakarta.validation.Valid;
import org.example.data.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/lists")
public class ListController {

    private final ListItemRepository itemRepository;
    private final ListRepository listRepository;

    public ListController(ListRepository listRepository, ListItemRepository itemRepository) {
        this.listRepository = listRepository;
        this.itemRepository = itemRepository;
    }

    // получить все листы
    @GetMapping
    public List<ListModel> getAllLists() {
        return listRepository.findAll();
    }

    // создать лист
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListModel createList(@Valid @RequestBody ListModel listModel) {
        ListModel savedList = listRepository.save(listModel); // сохраняем сам список

        if (listModel.getItems() != null && !listModel.getItems().isEmpty()) {
            for (ListItemModel item : listModel.getItems()) {
                item.setList(savedList); // привязываем к сохранённому списку
                itemRepository.save(item); // сохраняем каждый элемент
            }
        }
        return savedList;
    }

    // удалить лист
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteList(@PathVariable Long id) {
        ListModel list = listRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("List not found"));
        listRepository.deleteById(id);
        itemRepository.deleteByList(list);
    }

    /*
    TODO далее может добавится поиск списка по названию, либо по уникальному коду,
        либо добавлять пользователей в друзья, что бы иметь доступ в спискам.
        Нужно как то ограничить списки пользователей друг от друга.
     */
}
