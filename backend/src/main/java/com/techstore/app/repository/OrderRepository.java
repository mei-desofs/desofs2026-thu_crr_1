package com.techstore.app.repository;

import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.order.OrderStatus;
import com.techstore.app.domain.user.User;

import org.springframework.data.domain.Pageable; 
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderId;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, OrderId> {

    List<Order> findByCustomer(Customer customer);

    List<Order> findByCarrier(User carrier);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    @Query("""
    SELECT o FROM Order o
    JOIN o.customer c
    JOIN c.user u
    WHERE (CAST(:status AS string) IS NULL OR o.orderStatus = :status)
    AND (CAST(:email AS string) IS NULL OR u.email.email = :email)
    AND (CAST(:startDate AS timestamp) IS NULL OR o.createdAt >= :startDate)
    AND (CAST(:endDate AS timestamp) IS NULL OR o.createdAt <= :endDate)
""")
Page<Order> findAllWithFilters(
    @Param("status") String status,
    @Param("email") String email,
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate,
    Pageable pageable
);
}
