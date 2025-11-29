package org.example.data;

import jakarta.annotation.PostConstruct;
import org.example.data.model.ListItemModel;
import org.example.data.model.ListModel;
import org.example.data.model.UserModel;
import org.example.data.repository.ListItemRepository;
import org.example.data.repository.ListRepository;
import org.example.data.repository.UserRepository;

import org.example.security.service.PasswordService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final ListRepository listRepository;
    private final ListItemRepository itemRepository;
    private final PasswordService passwordService;

    public DataInitializer(UserRepository userRepository,
                           ListRepository listRepository,
                           ListItemRepository itemRepository,
                           PasswordService passwordService) {
        this.userRepository = userRepository;
        this.listRepository = listRepository;
        this.itemRepository = itemRepository;
        this.passwordService = passwordService;
    }

    @PostConstruct
    @Transactional
    public void init() {
        // Создаем администратора с новым хешем имени
        UserModel admin = new UserModel();
        admin.setName("admin");

        // Хешируем пароль (BCrypt)
        String hashedPassword = passwordService.hashPassword("admin");
        admin.setPasswordHash(hashedPassword);

        // Хешируем имя (SHA-256)
        String nameHash = passwordService.hashUsername("admin");
        admin.setNameHash(nameHash);

        System.out.println("=== CREATING ADMIN USER ===");
        System.out.println("Name: admin");
        System.out.println("NameHash: " + nameHash);
        System.out.println("PasswordHash: " + hashedPassword);

        // Генерируем начальный токен
        admin.setAuthToken(generateInitialToken());
        admin.setTokenExpiry(LocalDateTime.now().plusYears(1));
        admin.setLastLogin(LocalDateTime.now());
        admin.setActive(true);

        UserModel savedAdmin = userRepository.save(admin);
        System.out.println("Admin user saved with ID: " + savedAdmin.getId());
        System.out.println("===========================");

        // Создаем тестовые списки и элементы
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
                if (taken) {
                    item.setTakenByUser(admin);
                } else {
                    item.setTakenByUser(null);
                }
                itemRepository.save(item);
            }
        }

        // Дополнительно можно создать тестового пользователя
        createTestUser("user1", "password123");
        createTestUser("user2", "password456");
    }

    private void createTestUser(String username, String password) {
        if (userRepository.existsByNameHash(passwordService.hashUsername(username))) {
            return; // Пользователь уже существует
        }

        UserModel user = new UserModel();
        user.setName(username);

        String hashedPassword = passwordService.hashPassword(password);
        user.setPasswordHash(hashedPassword);

        String nameHash = passwordService.hashUsername(username);
        user.setNameHash(nameHash);

        user.setAuthToken(generateInitialToken());
        user.setTokenExpiry(LocalDateTime.now().plusYears(1));
        user.setLastLogin(LocalDateTime.now());
        user.setActive(true);

        userRepository.save(user);

        // Создаем тестовый список для пользователя
        ListModel list = new ListModel();
        list.setName(username + "_wishlist");
        list.setUser(user);
        listRepository.save(list);

        // Добавляем несколько элементов
        for (int i = 0; i < 5; i++) {
            ListItemModel item = new ListItemModel();
            item.setName(String.format("%s_item_%d", username, i));
            item.setList(list);
            item.setTaken(i % 2 == 0); // Каждый второй взят
            if (item.isTaken()) {
                item.setTakenByUser(user);
            }
            itemRepository.save(item);
        }
    }

    private String generateInitialToken() {
        return "init-token-" + System.currentTimeMillis() + "-" + Math.random();
    }
}