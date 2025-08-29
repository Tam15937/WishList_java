package org.example.controller;

import org.example.model.ListItemModel;
import org.example.model.ListModel;
import org.example.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ListItemController {

    private final WishlistService wishlistService;

    public ListItemController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    // Получить все предметы списка по id списка
    @GetMapping("/list/{listId}")
    public ResponseEntity<List<ListItemModel>> getItemsByListId(@PathVariable Long listId) {
        List<ListItemModel> items = wishlistService.findByUserId(listId)
                .stream()
                .filter(list -> list.getId().equals(listId))
                .findFirst()
                .map(ListModel::getItems)
                .orElse(List.of());

        return ResponseEntity.ok(items);
    }

    // Добавить предмет в список
    @PostMapping("/list/{listId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ListItemModel addItemToList(@PathVariable Long listId, @Valid @RequestBody ListItemModel item) {
        return wishlistService.addItemToList(listId, item);
    }

    // Удалить предмет по id предмета
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItemById(@PathVariable Long itemId) {
        boolean deleted = wishlistService.deleteItemById(itemId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Переключить статус taken у предмета по id предмета и пользователю
    @PostMapping("/{itemId}/toggle")
    public ResponseEntity<Void> toggleItemStatus(@PathVariable Long itemId, @RequestParam Long userId) {
        boolean toggled = wishlistService.toggleItemById(itemId, userId);
        if (toggled) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
