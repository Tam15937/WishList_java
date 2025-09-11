package org.example.data.service;

import org.example.data.model.ListItemModel;
import org.example.data.model.ListModel;
import org.example.data.model.UserModel;
import org.example.data.repository.ListItemRepository;
import org.example.data.repository.ListRepository;
import org.example.data.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {


    private final ListItemRepository itemRepository;


    public WishlistService( ListItemRepository itemRepository) {

        this.itemRepository = itemRepository;

    }

    public List<ListItemModel> findItemsByList(ListModel list) {
        return itemRepository.findByList(list);
    }

    @Transactional
    public void deleteItemsByList(ListModel list) {
        List<ListItemModel> items = itemRepository.findByList(list);
        itemRepository.deleteAll(items);
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



