package com.techstore.app.controller;

import com.techstore.app.config.ratelimit.annotation.RateLimit;
import com.techstore.app.dto.cart.CartItemDto;
import com.techstore.app.dto.cart.CartReponseDTO;
import com.techstore.app.dto.cart.UpdateCartItemDto;
import com.techstore.app.service.interfaces.CartItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartItemService cartItemService;

    @RateLimit("add-item-to-cart")
    @PostMapping("/items")
    public void addItemToCart(@Valid @RequestBody CartItemDto cartItemDto, HttpServletRequest request) {
        cartItemService.addItemToCart(cartItemDto, request);
    }

    @RateLimit("update-item-in-cart")
    @PostMapping("/items/{productId}")
    public void updateItemInCart(@PathVariable String productId,
            @Valid @RequestBody UpdateCartItemDto updateCartItemDto, HttpServletRequest request) {
        cartItemService.updateItemInCart(productId, updateCartItemDto, request);
    }

    @DeleteMapping("/items/{productId}")
    public void removeItemFromCart(@PathVariable String productId, HttpServletRequest request) {
        cartItemService.removeItemFromCart(productId, request);
    }

    @RateLimit("get-cart-items")
    @GetMapping()
    public CartReponseDTO getCartItems(HttpServletRequest request) {
        return cartItemService.getCartItems(request);
    }
}
