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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

        private final CartRepository cartRepository;
        private final CartAuditLogger cartAuditLogger;

        @Override
        public Cart createCart(Customer customer) {
                Cart cart = CartMapper.toEntity(customer);

                cartRepository.save(cart);

                cartAuditLogger.logCartCreation(
                                customer.getUser().getId().toString(),
                                cart.getId().toString());
                return cart;
        }

        @Override
        public void addNewItem(CartItem cartItem, CartId cartId) {
                
                Cart cart = cartRepository.findById(cartId)
                                .orElseThrow(() -> new BusinessException("Cart not found"));
                System.out.println(cartItem.getProduct().getId().toString());

                ProductId productId = cartItem.getProduct().getId();

                List<CartItem> items = Optional.ofNullable(cart.getItems())

        .orElseGet(ArrayList::new);

                CartItem existingItem = items.stream()
                                .filter(item -> item.getProduct().getId().equals(productId))
                                .findFirst()
                                .orElse(null);


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
        }

        @Override
        public void updateItem(ProductId productId, Integer quantityDelta, CartId cartId) {
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
        }

}
