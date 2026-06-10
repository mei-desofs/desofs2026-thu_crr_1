package com.techstore.app.domain.product;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.domain.shared.Quantity;
import com.techstore.app.exception.BusinessException;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "products")
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

    @Embedded
    private Quantity stockQuantity;

    @Column(nullable = true)
    private String imagePath;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public Product() {
    }

    public Product(String name, String description, Money price, Category category, Quantity stockQuantity) {
        this.id = ProductId.newId();
        this.name = new ProductName(name);
        this.description = new ProductDescription(description);
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
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

    public void decreaseStock(Quantity quantity) {
        if (this.stockQuantity.getQuantity() < quantity.getQuantity()) {
            throw new BusinessException("Not enough stock for product: " + this.name.getProductName());
        }
        this.stockQuantity = new Quantity(this.stockQuantity.getQuantity() - quantity.getQuantity());
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
