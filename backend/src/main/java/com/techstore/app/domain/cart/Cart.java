package com.techstore.app.domain.cart;

import com.techstore.app.domain.customer.Customer;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "carts")
public class Cart {

    @EmbeddedId
    private CartId id;

    @OneToMany(
    mappedBy = "cart",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
private List<CartItem> items;

    @OneToOne
    private Customer customer;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Cart() {
    }

    public Cart(Customer customer) {

    this.id = CartId.newId();

    this.customer = customer;

    this.items = new ArrayList<>();

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

    public void addItem(CartItem item) {

    item.attachTo(this);

    items.add(item);

}
}
