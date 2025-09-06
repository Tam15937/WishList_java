package org.example.controller;

import org.example.model.ListItemModel;
import org.example.model.ListModel;
import org.example.model.UserModel;
import org.example.repository.ListItemRepository;
import org.example.repository.ListRepository;
import org.example.repository.UserRepository;
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

    @GetMapping("/list/{listId}")
    public ResponseEntity<List<ListItemModel>> getItemsByListId(@PathVariable Long listId) {
        ListModel list = listRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));
        List<ListItemModel> items = wishlistService.findItemsByList(list);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/list/{listId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ListItemModel addItemToList(@PathVariable Long listId, @Valid @RequestBody ListItemModel item) {
        ListModel list = listRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));
        return wishlistService.addItemToList(list, item);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItemById(@PathVariable Long itemId) {
        ListItemModel item = itemRepository.findById(itemId)
                .orElse(null);
        if (item != null && wishlistService.deleteItem(item)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

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
}

