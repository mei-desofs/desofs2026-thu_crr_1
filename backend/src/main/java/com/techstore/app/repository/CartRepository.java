package com.techstore.app.repository;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartId;
import com.techstore.app.domain.customer.CustomerId;
import com.techstore.app.domain.user.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, CartId> {
    @Query("SELECT c FROM Cart c WHERE c.customer.user.email.email = :customerEmail")
    Cart findCartByCustomerEmail(String customerEmail);

    @Query("SELECT c FROM Cart c WHERE c.customer.user.supabaseUserId.id = :supabaseUserId")
    Optional<Cart> findBySupabaseUserId(UUID supabaseUserId);
}
