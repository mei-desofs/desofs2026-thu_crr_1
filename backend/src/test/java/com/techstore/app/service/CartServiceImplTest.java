package com.techstore.app.service;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartId;
import com.techstore.app.domain.cart.CartItem;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.product.ProductId;
import com.techstore.app.domain.shared.Quantity;
import com.techstore.app.domain.user.User;
import com.techstore.app.domain.user.UserId;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.logger.CartAuditLogger;
import com.techstore.app.mapper.CartMapper;
import com.techstore.app.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CartServiceImpl Tests")
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartAuditLogger cartAuditLogger;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private Customer customer;
    private User user;
    private Product product;
    private CartItem cartItem;
    private CartId cartId;
    private ProductId productId;

    @BeforeEach
    void setUp() {
        cartId = CartId.newId();
        productId = ProductId.newId();
        
        UserId userId = UserId.newId();
        user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        customer = mock(Customer.class);
        when(customer.getUser()).thenReturn(user);

        product = mock(Product.class);
        when(product.getId()).thenReturn(productId);

        cart = new Cart(customer);
        cart.getClass().getDeclaredFields(); 

        cartItem = new CartItem(1, product);
    }

    @Test
    @DisplayName("Should create cart successfully")
    void testCreateCartSuccess() {
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.createCart(customer);

        assertNotNull(result);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartAuditLogger, times(1)).logCartCreation(
                customer.getUser().getId().toString(),
                result.getId().toString());
    }

    @Test
    @DisplayName("Should handle exception when creating cart")
    void testCreateCartFailure() {
        when(cartRepository.save(any(Cart.class))).thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class, () -> cartService.createCart(customer));

        verify(cartAuditLogger, times(1)).logCartCreationFailure(
                customer.getUser().getId().toString(),
                "DB Error");
    }

    @Test
    @DisplayName("Should add new item to empty cart")
    void testAddNewItemToEmptyCart() {
        cart = new Cart(customer);
        cartItem = new CartItem(2, product);

        cartService.addNewItem(cartItem, cart);

        assertEquals(1, cart.getItems().size());
        verify(cartRepository, times(1)).save(cart);
        verify(cartAuditLogger, times(1)).logCartItemAdded(
                cart.getId().toString(),
                productId.toString(),
                2);
    }

    @Test
    @DisplayName("Should merge item quantity when product already exists in cart")
    void testAddNewItemMergeQuantity() {
        cart = new Cart(customer);
        CartItem existingItem = new CartItem(3, product);
        cart.addItem(existingItem);

        CartItem newItem = new CartItem(2, product);
        cartService.addNewItem(newItem, cart);

        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().get(0).getQuantity().getQuantity());
        verify(cartRepository, times(1)).save(cart);
        verify(cartAuditLogger, times(1)).logCartItemMerged(
                cart.getId().toString(),
                productId.toString(),
                2,
                5);
    }

    @Test
    @DisplayName("Should handle exception when adding item to cart")
    void testAddNewItemFailure() {
        cart = new Cart(customer);
        cartItem = new CartItem(1, product);

        doThrow(new RuntimeException("Save failed"))
                .when(cartRepository).save(any(Cart.class));

        assertThrows(BusinessException.class, 
                () -> cartService.addNewItem(cartItem, cart));

        verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                cart.getId().toString(),
                productId.toString(),
                "Save failed");
    }

    @Test
    @DisplayName("Should increment item quantity successfully")
    void testUpdateItemIncrementQuantity() {
        cart = new Cart(customer);
        CartItem existingItem = new CartItem(3, product);
        cart.addItem(existingItem);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        cartService.updateItem(productId, 2, cartId);

        assertEquals(5, cart.getItems().get(0).getQuantity().getQuantity());
        verify(cartRepository, times(1)).save(cart);
        verify(cartAuditLogger, times(1)).logCartUpdate(
                cart.getId().toString(),
                productId.toString(),
                5,
                "UPDATE");
    }

    @Test
    @DisplayName("Should decrement item quantity successfully")
    void testUpdateItemDecrementQuantity() {
        cart = new Cart(customer);
        CartItem existingItem = new CartItem(5, product);
        cart.addItem(existingItem);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        cartService.updateItem(productId, -2, cartId);

        assertEquals(3, cart.getItems().get(0).getQuantity().getQuantity());
        verify(cartRepository, times(1)).save(cart);
        verify(cartAuditLogger, times(1)).logCartUpdate(
                cart.getId().toString(),
                productId.toString(),
                3,
                "UPDATE");
    }

    @Test
    @DisplayName("Should throw exception when cart not found during update")
    void testUpdateItemCartNotFound() {
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, 
                () -> cartService.updateItem(productId, 1, cartId));

        verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                cartId.toString(),
                productId.toString(),
                "Cart not found");
    }

    @Test
    @DisplayName("Should throw exception when item not found during update")
    void testUpdateItemNotFound() {
        cart = new Cart(customer);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        assertThrows(BusinessException.class, 
                () -> cartService.updateItem(productId, 1, cartId));

        verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                cartId.toString(),
                productId.toString(),
                "Cart item not found");
    }

    @Test
    @DisplayName("Should remove item from cart successfully")
    void testRemoveItemSuccess() {
        cart = new Cart(customer);
        CartItem existingItem = new CartItem(2, product);
        cart.addItem(existingItem);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        cartService.removeItem(productId, cartId);

        assertEquals(0, cart.getItems().size());
        verify(cartRepository, times(1)).save(cart);
        verify(cartAuditLogger, times(1)).logCartUpdate(
                cart.getId().toString(),
                productId.toString(),
                0,
                "REMOVE");
    }

    @Test
    @DisplayName("Should throw exception when cart not found during remove")
    void testRemoveItemCartNotFound() {
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, 
                () -> cartService.removeItem(productId, cartId));

        verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                cartId.toString(),
                productId.toString(),
                "Cart not found");
    }

    @Test
    @DisplayName("Should throw exception when item not found during remove")
    void testRemoveItemNotFound() {
        cart = new Cart(customer);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        assertThrows(BusinessException.class, 
                () -> cartService.removeItem(productId, cartId));

        verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                cartId.toString(),
                productId.toString(),
                "Cart item not found");
    }

    @Test
    @DisplayName("Should handle zero quantity delta")
    void testUpdateItemZeroDelta() {
        cart = new Cart(customer);
        CartItem existingItem = new CartItem(3, product);
        cart.addItem(existingItem);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        cartService.updateItem(productId, 0, cartId);

        assertEquals(3, cart.getItems().get(0).getQuantity().getQuantity());
        verify(cartRepository, times(1)).save(cart);
    }
}