package com.techstore.app.domain.order;

import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.shared.Address;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.domain.user.User;
import com.techstore.app.exception.BusinessException;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "orders")
public class Order {

    @Version
    private Long version;

    @EmbeddedId
    private OrderId id;

    @Embedded
    private Money totalPrice;

    @Embedded
    private Address address;

    private OrderStatus orderStatus;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "carrier_id", nullable = true)
    private User carrier;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Order() {
    }

    public Order(BigDecimal totalPrice, String postalCode, String city, String country, String street,
            OrderStatus orderStatus, List<OrderItem> orderItems) {
        if (!validate(orderItems)) {
            throw new BusinessException("An order must contain at least one item.");
        }
        this.id = OrderId.newId();
        this.totalPrice = new Money(totalPrice);
        this.address = new Address(postalCode, city, country, street);
        this.orderStatus = orderStatus;
        this.orderItems = orderItems;
    }

    public Order(BigDecimal totalPrice, String postalCode, String city, String country, String street,
            OrderStatus orderStatus, List<OrderItem> orderItems, Customer customer) {
        if (!validate(orderItems)) {
            throw new BusinessException("An order must contain at least one item.");
        }
        this.id = OrderId.newId();
        this.totalPrice = new Money(totalPrice);
        this.address = new Address(postalCode, city, country, street);
        this.orderStatus = orderStatus;
        this.orderItems = orderItems;
        this.customer = customer;
    }

    private boolean validate(List<OrderItem> orderItems) {
        return orderItems != null && orderItems.size() > 0;
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

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void pickup(User carrier) {

        if(carrier == null){
            throw new BusinessException("Order's carrier cannot be null.");
        }

        if (this.orderStatus != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Order cannot be picked up: current status is " + this.orderStatus);
        }
        if (this.carrier != null) {
            throw new BusinessException("Order already has a carrier assigned.");
        }
        this.carrier = carrier;
        this.orderStatus = OrderStatus.PICKED_UP;
    }
}
