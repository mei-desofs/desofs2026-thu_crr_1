package com.techstore.app.controller;

import com.techstore.app.config.ratelimit.annotation.RateLimit;
import com.techstore.app.dto.cart.CartItemDto;
import com.techstore.app.dto.cart.CartProductResponseDto;
import com.techstore.app.dto.cart.UpdateCartItemDto;
import com.techstore.app.service.interfaces.CartItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartItemService cartItemService;

    @GetMapping("/items")
    public ResponseEntity<List<CartProductResponseDto>> getAllCartItems(HttpServletRequest request) {
        List<CartProductResponseDto> items = cartItemService.getAllCartItems(request);
        return ResponseEntity.ok(items);
    }
    
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

    @RateLimit("delete-order")
    @DeleteMapping("/items/{productId}")
    public void removeItemFromCart(@PathVariable String productId, HttpServletRequest request) {
        cartItemService.removeItemFromCart(productId, request);
    }
}
