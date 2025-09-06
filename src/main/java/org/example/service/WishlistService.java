package org.example.service;

import org.example.model.ListItemModel;
import org.example.model.ListModel;
import org.example.model.UserModel;
import org.example.repository.ListItemRepository;
import org.example.repository.ListRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    private final ListRepository listRepository;
    private final ListItemRepository itemRepository;
    private final UserRepository userRepository;

    public WishlistService(ListRepository listRepository, ListItemRepository itemRepository, UserRepository userRepository) {
        this.listRepository = listRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public List<ListModel> findListsByUser(UserModel user) {
        return listRepository.findByUser(user);
    }

    @Transactional
    public ListModel saveWishlist(ListModel wishlist) {
        if (wishlist.getItems() != null) {
            wishlist.getItems().forEach(item -> item.setList(wishlist));
        }
        return listRepository.save(wishlist);
    }

    @Transactional
    public boolean deleteWishlist(ListModel list, UserModel user) {
        if (list.getUser().equals(user)) {
            listRepository.delete(list);
            return true;
        }
        return false;
    }

    public List<ListItemModel> findItemsByList(ListModel list) {
        return itemRepository.findByList(list);
    }

    @Transactional
    public ListItemModel addItemToList(ListModel list, ListItemModel item) {
        item.setList(list);
        return itemRepository.save(item);
    }

    @Transactional
    public boolean deleteItem(ListItemModel item) {
        if (itemRepository.existsById(item.getId())) {
            itemRepository.delete(item);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean toggleItem(ListItemModel item, UserModel currentUser) {
        if (item.isTaken()) {
            if (item.getTakenByUser() != null && item.getTakenByUser().equals(currentUser)) {
                item.setTaken(false);
                item.setTakenByUser(null);
            } else {
                return false;
            }
        } else {
            item.setTaken(true);
            item.setTakenByUser(currentUser);
        }
        itemRepository.save(item);
        return true;
    }
}



