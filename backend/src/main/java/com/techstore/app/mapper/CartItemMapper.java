package com.techstore.app.mapper;

import com.techstore.app.domain.cart.CartItem;
import com.techstore.app.domain.product.Product;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {

    public static CartItem toEntity(Integer quantity, Product product) {
        return new CartItem(
                quantity,
                product
        );
    }
}
