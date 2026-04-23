package com.techstore.app.domain.product;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.exception.BusinessException;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@Entity
public class Product {

    @EmbeddedId
    private ProductId id;

    @Embedded
    private ProductName name;

    @Embedded
    private ProductDescription description;

    @Embedded
    private Money price;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Category category;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Product() {}

    public Product(String name, String description, Money price, Category category) {
        this.id = ProductId.newId();
        this.name = new ProductName(name);
        this.description = new ProductDescription(description);
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
}
