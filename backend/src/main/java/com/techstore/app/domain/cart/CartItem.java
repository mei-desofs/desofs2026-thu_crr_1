package com.techstore.app.domain.cart;

import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Quantity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "cart_items")
public class CartItem {

    @EmbeddedId
    private CartItemId id;

    @Embedded
    private Quantity quantity;

    @OneToOne
    @JoinColumn(nullable = false)
    private Product product;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CartItem() {}

    public CartItem(Integer quantity, Product product) {
        this.id = CartItemId.newId();
        this.quantity = new Quantity(quantity);
        this.product = product;
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
