package org.example;

import jakarta.annotation.PostConstruct;
import org.example.model.ListItemModel;
import org.example.model.ListModel;
import org.example.model.UserModel;
import org.example.repository.ListItemRepository;
import org.example.repository.ListRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final ListRepository listRepository;
    private final ListItemRepository itemRepository;

    public DataInitializer(UserRepository userRepository,
                           ListRepository listRepository,
                           ListItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.listRepository = listRepository;
        this.itemRepository = itemRepository;
    }

    @PostConstruct
    @Transactional
    public void init() {
        UserModel admin = new UserModel();
        admin.setName("admin");
        admin.setPassword("admin");
        userRepository.save(admin);

        for (int i = 0; i < 2; i++) {
            ListModel list = new ListModel();
            list.setName(String.format("admin_list_%d", i));
            list.setUser(admin);
            listRepository.save(list);

            for (int j = 0; j < 10; j++) {
                ListItemModel item = new ListItemModel();
                item.setName(String.format("admin_item_%d", j));
                item.setList(list);
                boolean taken = (j % 4) < 2; // первые два не взяты, следующие два взяты и т.д.
                item.setTaken(taken);
                itemRepository.save(item);
            }
        }
    }
}