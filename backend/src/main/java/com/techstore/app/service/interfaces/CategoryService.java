package com.techstore.app.service.interfaces;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.category.CategoryName;

public interface CategoryService {

    Category save(Category category);
    Category findByName(CategoryName name);
}
