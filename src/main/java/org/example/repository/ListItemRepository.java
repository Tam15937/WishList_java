package org.example.repository;

import org.example.model.ListItemModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListItemRepository extends JpaRepository<ListItemModel, Long> {
    List<ListItemModel> findByListIdOrderById(Long listId);
    void deleteByListIdAndId(Long listId, Long id);
}
