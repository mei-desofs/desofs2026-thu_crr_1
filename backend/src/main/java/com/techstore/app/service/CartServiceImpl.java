package com.techstore.app.service;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartId;
import com.techstore.app.domain.cart.CartItem;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.product.ProductId;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.logger.CartAuditLogger;
import com.techstore.app.mapper.CartMapper;
import com.techstore.app.repository.CartRepository;
import com.techstore.app.service.interfaces.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartAuditLogger cartAuditLogger;

    @Override
    public Cart createCart(Customer customer) {
        try {
            Cart cart = CartMapper.toEntity(customer);

            cartRepository.save(cart);

            cartAuditLogger.logCartCreation(
                    customer.getUser().getId().toString(),
                    cart.getId().toString());
            
            return cart;
            
        } catch (Exception e) {
            cartAuditLogger.logCartCreationFailure(
                    customer.getUser().getId().toString(),
                    e.getMessage());
            throw e;
        }
    }

    @Override
    public void addNewItem(CartItem cartItem, Cart cart) {
        ProductId productId = cartItem.getProduct().getId();
        List<CartItem> items = cart.getItems();

        CartItem existingItem = items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        try {
            if (existingItem != null) {
                int addedQty = cartItem.getQuantity().getQuantity();
                int oldQty = existingItem.getQuantity().getQuantity();

                existingItem.getQuantity().incrementQuantity(addedQty);

                cartAuditLogger.logCartItemMerged(
                        cart.getId().toString(),
                        productId.toString(),
                        addedQty,
                        oldQty + addedQty);
            } else {
                cart.addItem(cartItem);

                cartAuditLogger.logCartItemAdded(
                        cart.getId().toString(),
                        productId.toString(),
                        cartItem.getQuantity().getQuantity());
            }

            cartRepository.save(cart);

            cartAuditLogger.logCartUpdate(
                    cart.getId().toString(),
                    productId.toString(),
                    cartItem.getQuantity().getQuantity(),
                    existingItem != null ? "MERGE" : "ADD");
                    
        } catch (Exception e) {
            cartAuditLogger.logCartUpdateFailure(
                    cart.getId().toString(),
                    productId.toString(),
                    e.getMessage());
            throw new BusinessException("Failed to add item to cart: " + e.getMessage());
        }
    }

    @Override
    public void updateItem(ProductId productId, Integer quantityDelta, CartId cartId) {
        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new BusinessException("Cart not found"));

            CartItem existingItem = cart.getItems()
                    .stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Cart item not found"));

            int oldQty = existingItem.getQuantity().getQuantity();

            if (quantityDelta > 0) {
                existingItem.getQuantity().incrementQuantity(quantityDelta);
            } else if (quantityDelta < 0) {
                existingItem.getQuantity().decrementQuantity(Math.abs(quantityDelta));
            }

            cartRepository.save(cart);

            cartAuditLogger.logCartUpdate(
                    cart.getId().toString(),
                    productId.toString(),
                    existingItem.getQuantity().getQuantity(),
                    "UPDATE");
                    
        } catch (BusinessException e) {
            cartAuditLogger.logCartUpdateFailure(
                    cartId.toString(),
                    productId.toString(),
                    e.getMessage());
            throw e;
        }
    }

    @Override
    public void removeItem(ProductId productId, CartId cartId) {
        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new BusinessException("Cart not found"));

            CartItem itemToRemove = cart.getItems()
                    .stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Cart item not found"));

            cart.getItems().remove(itemToRemove);

            cartRepository.save(cart);

            cartAuditLogger.logCartUpdate(
                    cart.getId().toString(),
                    productId.toString(),
                    0,
                    "REMOVE");
                    
        } catch (BusinessException e) {
            cartAuditLogger.logCartUpdateFailure(
                    cartId.toString(),
                    productId.toString(),
                    e.getMessage());
            throw e;
        }
    }
}