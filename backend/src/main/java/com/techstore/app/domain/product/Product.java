package com.techstore.app.domain.product;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.exception.BusinessException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Embedded
    private Money price;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Category category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Product() {}

    public Product(String name, String description, Money price, Category category) {
        if (!isNameValid(name)) {
            throw new BusinessException("Product name must contain only letters.");
        }
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
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

    public void setName(String name) {
        if (!isNameValid(name)) {
            throw new BusinessException("Product name must contain only letters.");
        }
        this.name = name;
    }

    public boolean isNameValid(String name) {
        return name.matches("[a-zA-Z0-9 ]+") && !name.isBlank();
    }
}
