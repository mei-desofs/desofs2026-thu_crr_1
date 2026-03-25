package com.techstore.app.service.interfaces;

import com.techstore.app.domain.category.Category;

public interface CategoryService {

    Category save(Category category);
    Category findByName(String name);
}
