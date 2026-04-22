package com.techstore.app.domain.cart;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "carts")
public class Cart {

    @EmbeddedId
    private CartId id;

    @OneToMany
    private List<CartItem> items;

//    @OneToOne
//    private Customer customer;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Cart() {}

    public Cart(List<CartItem> items) {
        this.id = CartId.newId();
        this.items = items;
    }

    private boolean validate(List<CartItem> items) {
        return items != null && items.size() > 0;
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
