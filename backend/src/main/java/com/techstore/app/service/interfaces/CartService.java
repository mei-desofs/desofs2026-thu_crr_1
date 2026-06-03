package com.techstore.app.service.interfaces;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartId;
import com.techstore.app.domain.cart.CartItem;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.product.ProductId;

public interface CartService {
    Cart createCart(Customer customer);
    void addNewItem(CartItem cartItem, CartId cartId);
    void updateItem(ProductId productId, Integer quantityDelta, CartId cartId);
}
