package com.techstore.app.service;

import com.techstore.app.domain.category.Category;
import com.techstore.app.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void shouldSaveCategoryWhenNameIsUnique() {
        Category input = new Category("Storage");
        Category saved = new Category("Storage");
        saved.setId(1L);

        when(categoryRepository.findByName("Storage")).thenReturn(null);
        when(categoryRepository.save(input)).thenReturn(saved);

        Category result = categoryService.save(input);

        assertEquals(saved, result);
    }

    @Test
    void shouldThrowWhenCategoryNameAlreadyExists() {
        Category input = new Category("Storage");

        when(categoryRepository.findByName("Storage")).thenReturn(new Category("Storage"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> categoryService.save(input));

        assertEquals("Category with name 'Storage' already exists.", exception.getMessage());
    }

    @Test
    void shouldFindCategoryByName() {
        Category category = new Category("Peripherals");

        when(categoryRepository.findByName("Peripherals")).thenReturn(category);

        Category result = categoryService.findByName("Peripherals");

        assertEquals(category, result);
    }
}
