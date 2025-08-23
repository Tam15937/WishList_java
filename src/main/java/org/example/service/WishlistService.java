package org.example.service;

import org.example.model.Wishlist;
import org.example.model.WishlistItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private final List<Wishlist> wishlists = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    // Возвращает списки по имени пользователя
    public List<Wishlist> findByUsername(String username) {
        lock.lock();
        try {
            // Можно вернуть все списки пользователя или все вообще (зависит от бизнес-логики)
            return wishlists.stream()
                    .filter(w -> w.getUsername().equals(username))
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    // Добавляет/создает новый список
    public void addWishlist(Wishlist wishlist) {
        lock.lock();
        try {
            wishlists.add(wishlist);
        } finally {
            lock.unlock();
        }
    }

    // Удаляет список по ID, если username совпадает
    public boolean deleteWishlist(long id, String username) {
        lock.lock();
        try {
            return wishlists.removeIf(w -> w.getId() == id && w.getUsername().equals(username));
        } finally {
            lock.unlock();
        }
    }

    // Переключает статус taken у элемента списка
    public boolean toggleItem(long listId, int itemIndex, long currentUserId) {
        lock.lock();
        try {
            for (Wishlist w : wishlists) {
                if (w.getId() == listId) {
                    List<WishlistItem> items = w.getWishlist();
                    if (itemIndex < 0 || itemIndex >= items.size()) {
                        return false;
                    }

                    WishlistItem item = items.get(itemIndex);
                    boolean currentTaken = item.isTaken();

                    if (currentTaken) {
                        // Если снимаем отметку, то можно лишь если currentUserId владелец отметки
                        if (item.getTakenByUserId() != null && item.getTakenByUserId() == currentUserId) {
                            item.setTaken(false);
                            item.setTakenByUserId(null);
                        } else {
                            return false;
                        }
                    } else {
                        // Устанавливаем отметку
                        item.setTaken(true);
                        item.setTakenByUserId(currentUserId);
                    }
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
}
