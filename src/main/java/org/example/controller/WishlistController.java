package org.example.controller;

import org.example.model.Wishlist;
import org.example.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/api/users")
    public ResponseEntity<List<Wishlist>> getUsersWishlist(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        List<Wishlist> wishlists = wishlistService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(wishlists);
    }

    @PostMapping("/api/create_list")
    public ResponseEntity<?> createList(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody Wishlist wishlist) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        wishlist.setUsername(userDetails.getUsername());
        // Генерация ID, если нужно (можно заменить на более надежный способ)
        if (wishlist.getId() == 0) {
            wishlist.setId(System.currentTimeMillis());
        }
        wishlistService.addWishlist(wishlist);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/delete_list/{id}")
    public ResponseEntity<?> deleteList(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable long id) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        boolean deleted = wishlistService.deleteWishlist(id, userDetails.getUsername());
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(403).body("Недостаточно прав для удаления списка");
        }
    }

    @MessageMapping("/wishlist/update")
    @SendTo("/topic/wishlist")
    public String handleWishlistUpdate(String message) {
        // Тут можно обработать сообщение, например, сохранить обновление
        return message; // Ответ будет отправлен
    }
}
