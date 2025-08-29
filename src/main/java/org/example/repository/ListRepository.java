package org.example.repository;

import org.example.model.ListModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListRepository extends JpaRepository<ListModel, Long> {
    List<ListModel> findByUserId(Long userId);
}
