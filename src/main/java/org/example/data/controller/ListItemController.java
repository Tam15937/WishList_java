package org.example.data.controller;

import org.example.data.model.ListItemModel;
import org.example.data.model.ListModel;
import org.example.data.model.UserModel;
import org.example.data.repository.ListItemRepository;
import org.example.data.repository.ListRepository;
import org.example.data.repository.UserRepository;
import org.example.data.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
@RestController
@RequestMapping("/items")
public class ListItemController {

    private final WishlistService wishlistService;
    private final ListRepository listRepository;
    private final ListItemRepository itemRepository;
    private final UserRepository userRepository;

    public ListItemController(WishlistService wishlistService,
                              ListRepository listRepository,
                              ListItemRepository itemRepository,
                              UserRepository userRepository) {
        this.wishlistService = wishlistService;
        this.listRepository = listRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    // подгрузка элементов списка по id списка
    @GetMapping("/list/{listId}")
    public ResponseEntity<List<ListItemModel>> getItemsByListId(
            @PathVariable Long listId,
            @CookieValue(name = "user_id", required = false) Long currentUserId) {

        ListModel list = listRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));
        List<ListItemModel> items = wishlistService.findItemsByList(list);

        // Проходим по списку и помечаем "мои" предметы
        if (currentUserId != null) {
            items.forEach(item -> {
                if (item.isTaken() && item.getTakenByUser() != null) {
                    // Если ID владельца совпадает с ID из куки (или сессии)
                    item.setMine(item.getTakenByUser().getId().equals(currentUserId));
                }
            });
        }

        // Сортируем по уникальному id (например, getId)
        items.sort(Comparator.comparing(ListItemModel::getId));
        return ResponseEntity.ok(items);
    }

    // добавление предмета
    @PostMapping("/list/{listId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ListItemModel addItemToList(@PathVariable Long listId, @Valid @RequestBody ListItemModel item) {
        ListModel list = listRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));
        return wishlistService.addItemToList(list, item);
    }

    // удаление предмета
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItemById(@PathVariable Long itemId) {
        ListItemModel item = itemRepository.findById(itemId)
                .orElse(null);
        if (item != null && wishlistService.deleteItem(item)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // переключение статуса предмета (отмечен / не отмечен)
    @PostMapping("/{itemId}/toggle")
    public ResponseEntity<Void> toggleItemStatus(@PathVariable Long itemId, @RequestParam Long userId) {
        ListItemModel item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean toggled = wishlistService.toggleItem(item, user);
        if (toggled) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /*
        TODO по идее, больше предметы не должны ничего делать.
     */
}

