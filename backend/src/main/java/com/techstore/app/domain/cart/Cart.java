package com.techstore.app.domain.cart;

import com.techstore.app.domain.customer.Customer;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.stream.Collectors;

import com.techstore.app.domain.order.OrderItem;

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

    public Cart(List<CartItem> items, Customer customer) {
        this.id = CartId.newId();
        this.items = items;
        this.customer = customer;
    }
  
    public Cart(Customer customer) {
        this.id = CartId.newId();
        this.customer = customer;
        this.items = new ArrayList<>();
    }

    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (this.items == null || this.items.isEmpty()) {
            return total;
        }
        for (CartItem item : this.items) {
            BigDecimal price = item.getProduct().getPrice().getMoneyValue();
            BigDecimal qty = BigDecimal.valueOf(item.getQuantity().getQuantity());
            total = total.add(price.multiply(qty));
        }
        return total;
    }

    public List<OrderItem> toOrderItems() {
        if (this.items == null) {
            return List.of();
        }
        return this.items.stream()
                .map(item -> new OrderItem(item.getQuantity().getQuantity(),
                        item.getProduct().getPrice().getMoneyValue(),
                        item.getProduct()))
                .collect(Collectors.toList());
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

    public void clearItems() {
        if (this.items != null) {
            this.items.clear();
        }
    }

    public void addItem(CartItem item) {

        item.attachTo(this);

        items.add(item);

    }
}
