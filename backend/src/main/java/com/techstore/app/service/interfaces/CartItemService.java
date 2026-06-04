package com.techstore.app.service.interfaces;

import com.techstore.app.dto.cart.CartItemDto;
import com.techstore.app.dto.cart.UpdateCartItemDto;
import jakarta.servlet.http.HttpServletRequest;

public interface CartItemService {
    void addItemToCart(CartItemDto cartItemDto, HttpServletRequest request);
    void updateItemInCart(String productId,UpdateCartItemDto cartItemDto, HttpServletRequest request);
}
