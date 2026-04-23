package com.techstore.app.domain.category;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "categories")
public class Category {

    @EmbeddedId
    private CategoryId id;

    @Embedded
    @Column(nullable = false, unique = true)
    private CategoryName name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Category() {}

    public Category(String name) {
        if (!isValid(name)) {
            throw new BusinessException("Category name must contain only letters.");
        }

        this.id = CategoryId.newId();
        this.name = new CategoryName(name);
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isValid(String name) {
        return name.matches("[a-zA-Z0-9 ]+") && !name.isBlank();
    }
}
