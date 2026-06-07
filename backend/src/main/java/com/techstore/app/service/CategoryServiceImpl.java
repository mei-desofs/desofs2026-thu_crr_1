package com.techstore.app.service;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.category.CategoryName;
import com.techstore.app.dto.category.CategoryResponseDTO;
import com.techstore.app.repository.CategoryRepository;
import com.techstore.app.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Category save(Category category) {
        if (findByName(category.getName()) != null) {
            throw new IllegalArgumentException("Category with name '" + category.getName().getCategoryName() + "' already exists.");
        }

        return categoryRepository.save(category);
    }

    @Override
    public Category findByName(CategoryName name) {
        return categoryRepository.findByName(name);
    }

    @Override
    public List<CategoryResponseDTO> findAll() {
        var categories = categoryRepository.findAll();
        return categories.stream()
                .map(category -> new CategoryResponseDTO(category.getId().getId(), category.getName().getCategoryName()))
                .toList();
    }
}
