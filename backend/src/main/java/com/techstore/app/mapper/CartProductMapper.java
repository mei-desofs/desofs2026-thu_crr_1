package com.techstore.app.mapper;

import com.techstore.app.domain.cart.CartItem;
import com.techstore.app.domain.product.Product;
import com.techstore.app.dto.cart.CartProductResponseDto;

public class CartProductMapper {

    public static CartProductResponseDto toResponseDto(CartItem cartItem, Product product) {
        return new CartProductResponseDto(
                product.getId().getId().toString(),
                product.getName().getProductName(),
                product.getDescription().getDescription(),
                cartItem.getQuantity().getQuantity(),
                product.getPrice().getMoneyValue()
        );
    }
}