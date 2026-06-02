package com.techstore.app.repository;

import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.customer.CustomerId;
import com.techstore.app.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, CustomerId> {

    Optional<Customer> findByUser(User user);

    boolean existsByUser(User user);
}