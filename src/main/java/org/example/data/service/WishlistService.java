package org.example.data.service;

import org.example.data.model.ListItemModel;
import org.example.data.model.ListModel;
import org.example.data.model.UserModel;
import org.example.data.repository.ListItemRepository;
import org.example.data.repository.ListRepository;
import org.example.data.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service
public class WishlistService {


    private final ListItemRepository itemRepository;
    private final ListRepository listRepository;

    public WishlistService( ListItemRepository itemRepository, ListRepository listRepository) {

        this.itemRepository = itemRepository;
        this.listRepository = listRepository;

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
    // Сравнение по уникальным полям (name, link)
    private boolean itemsAreEqual(ListItemModel item1, ListItemModel item2) {
        if (!item1.getName().equals(item2.getName())) {
            return false;
        }

        String link1 = item1.getLink();
        String link2 = item2.getLink();

        // null и пустая строка считаются равными
        if ((link1 == null || link1.isEmpty()) && (link2 == null || link2.isEmpty())) {
            return true;
        }

        boolean link1Valid = link1 != null && link1.toLowerCase().startsWith("http");
        boolean link2Valid = link2 != null && link2.toLowerCase().startsWith("http");

        if (link1Valid && link2Valid) {
            return link1.equals(link2);
        }

        // Если хотя бы одна ссылка невалидна, считаем одинаковыми (безразлично)
        return true;
    }


    @Transactional
    public void updateWishlistItems(ListModel list, List<ListItemModel> newItems) {
        List<ListItemModel> currentItems = list.getItems();

        // Добавление новых элементов (те, что отсутствуют в currentItems)
        for (ListItemModel newItem : newItems) {
            boolean exists = currentItems.stream()
                    .anyMatch(existingItem -> itemsAreEqual(existingItem, newItem));
            if (!exists) {
                newItem.setList(list);
                currentItems.add(newItem);
            }
        }

        // Удаление из currentItems элементов, которых нет в newItems
        Iterator<ListItemModel> iter = currentItems.iterator();
        while (iter.hasNext()) {
            ListItemModel existingItem = iter.next();
            boolean stillExists = newItems.stream()
                    .anyMatch(newItem -> itemsAreEqual(existingItem, newItem));
            if (!stillExists) {
                iter.remove();  // удаляем из коллекции, Hibernate выполнит удаление из БД
            }
        }

        // Сохраняем список, чтобы Hibernate применил изменения
        listRepository.save(list);
    }

}



