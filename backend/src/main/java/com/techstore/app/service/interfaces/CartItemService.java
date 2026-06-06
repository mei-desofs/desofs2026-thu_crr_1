package com.techstore.app.service.interfaces;

import java.util.List;

import com.techstore.app.dto.cart.CartItemDto;
import com.techstore.app.dto.cart.CartProductResponseDto;
import com.techstore.app.dto.cart.UpdateCartItemDto;
import jakarta.servlet.http.HttpServletRequest;

public interface CartItemService {
    void addItemToCart(CartItemDto cartItemDto, HttpServletRequest request);
    void updateItemInCart(String productId,UpdateCartItemDto cartItemDto, HttpServletRequest request);
    void removeItemFromCart(String productId, HttpServletRequest request);
    List<CartProductResponseDto> getAllCartItems(HttpServletRequest request);

}
