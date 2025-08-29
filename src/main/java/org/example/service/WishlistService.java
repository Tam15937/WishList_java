package org.example.service;

import org.example.model.ListItemModel;
import org.example.model.ListModel;
import org.example.repository.ListItemRepository;
import org.example.repository.ListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    private final ListRepository listRepository;
    private final ListItemRepository itemRepository;

    public WishlistService(ListRepository listRepository, ListItemRepository itemRepository) {
        this.listRepository = listRepository;
        this.itemRepository = itemRepository;
    }

    // Получить все списки пользователя по userId
    public List<ListModel> findByUserId(Long userId) {
        return listRepository.findByUserId(userId);
    }

    // Создать или обновить список вместе с элементами
    @Transactional
    public ListModel addWishlist(ListModel wishlist) {
        if (wishlist.getItems() != null) {
            wishlist.getItems().forEach(item -> item.setList(wishlist));
        }
        return listRepository.save(wishlist);
    }

    // Удалить список по id и userId
    @Transactional
    public boolean deleteWishlist(Long id, Long userId) {
        Optional<ListModel> listOpt = listRepository.findById(id);
        if (listOpt.isPresent() && listOpt.get().getUserId().equals(userId)) {
            listRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Добавить предмет в список
    @Transactional
    public ListItemModel addItemToList(Long listId, ListItemModel item) {
        ListModel list = listRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));
        item.setList(list);
        return itemRepository.save(item);
    }

    // Удалить предмет по id
    @Transactional
    public boolean deleteItemById(Long itemId) {
        if (itemRepository.existsById(itemId)) {
            itemRepository.deleteById(itemId);
            return true;
        }
        return false;
    }

    // Переключить статус taken у предмета списка
    @Transactional
    public boolean toggleItemById(Long itemId, Long currentUserId) {
        Optional<ListItemModel> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            return false;
        }

        ListItemModel item = optionalItem.get();

        if (item.isTaken()) {
            // Снимаем метку, только если текущий пользователь - владелец метки
            if (item.getTakenByUserId() != null && item.getTakenByUserId().equals(currentUserId)) {
                item.setTaken(false);
                item.setTakenByUserId(null);
            } else {
                return false;
            }
        } else {
            // Ставим метку
            item.setTaken(true);
            item.setTakenByUserId(currentUserId);
        }

        itemRepository.save(item);
        return true;
    }

}
