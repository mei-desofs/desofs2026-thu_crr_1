package com.techstore.app.service.interfaces;

import java.util.List;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.category.CategoryName;
import com.techstore.app.dto.category.CategoryResponseDTO;

public interface CategoryService {

    Category save(Category category);

    Category findByName(CategoryName name);

    List<CategoryResponseDTO> findAll();
}
