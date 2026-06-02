package com.techstore.app.repository;

import com.techstore.app.domain.cart.CartItem;
import com.techstore.app.domain.cart.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
}
