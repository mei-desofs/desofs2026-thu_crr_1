package com.techstore.app.repository;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.category.CategoryId;
import com.techstore.app.domain.category.CategoryName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, CategoryId> {
    Category findByName(CategoryName name);
}
