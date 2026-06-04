package com.techstore.app.repository;

import com.techstore.app.domain.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderId;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, OrderId> {
    List<Order> findByCustomer(Customer customer);
}
