package com.techstore.app.bootstrapping;

import com.techstore.app.domain.category.Category;
import com.techstore.app.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Profile("bootstrap")
public class Bootstrapper implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrapper.class);

    private final CategoryService categoryService;

    @Override
    @Transactional
    public void run(final String... args) {
        LOGGER.info("Starting application bootstrap...");

        createCategories();

        LOGGER.info("Bootstrap completed successfully.");
    }

    private void createCategories() {
        if (categoryService.findByName("Technology") == null) {
            categoryService.save(new Category("Technology"));
        }

        if  (categoryService.findByName("Electronics") == null) {
            categoryService.save(new Category("Electronics"));
        }

        if (categoryService.findByName("Computers") == null) {
            categoryService.save(new Category("Computers"));
        }

        if (categoryService.findByName("Accessories") == null) {
            categoryService.save(new Category("Accessories"));
        }

        LOGGER.info("Categories created successfully.");
    }
}
