package org.example.data.repository;

import org.example.data.model.ListItemModel;
import org.example.data.model.ListModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListItemRepository extends JpaRepository<ListItemModel, Long> {
    List<ListItemModel> findByList(ListModel list);
}
