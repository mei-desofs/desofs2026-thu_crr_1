package com.techstore.app.mapper;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartItem;
import com.techstore.app.domain.customer.Customer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartMapper {

    public static Cart toEntity(Customer customer) {
        return new Cart(
            customer
        );
    }
}
