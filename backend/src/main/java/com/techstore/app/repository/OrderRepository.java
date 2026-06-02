package com.techstore.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderId;

public interface OrderRepository extends JpaRepository<Order, OrderId> {
}
