package com.techstore.app.repository.Custom;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderStatus;
import com.techstore.app.domain.user.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class OrderSpecification {

    public static Specification<Order> withFilters(
            OrderStatus status,
            String email,
            LocalDate startDate,
            LocalDate endDate) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Order, Customer> customer = root.join("customer", JoinType.INNER);
            Join<Customer, User> user = customer.join("user", JoinType.INNER);

            if (status != null) {
                predicates.add(cb.equal(root.get("orderStatus"), status));
            }
            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(user.get("email").get("email")),
                        "%" + email.toLowerCase() + "%"));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"), endDate.atTime(23, 59, 59)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
