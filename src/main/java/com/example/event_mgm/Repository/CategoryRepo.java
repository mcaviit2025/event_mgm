package com.example.event_mgm.Repository;

import com.example.event_mgm.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category,Long> {

    Optional<Category> findByName(String name);
}
