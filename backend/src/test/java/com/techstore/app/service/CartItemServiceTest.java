package com.techstore.app.service;

import com.techstore.app.config.FileUploadConfig;
import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartId;
import com.techstore.app.domain.cart.CartItem;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.product.ProductId;
import com.techstore.app.domain.product.ProductName;
import com.techstore.app.domain.product.ProductDescription;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import com.techstore.app.domain.user.UserId;
import com.techstore.app.domain.user.Email;
import com.techstore.app.dto.cart.CartItemDto;
import com.techstore.app.dto.cart.CartProductResponseDto;
import com.techstore.app.dto.cart.UpdateCartItemDto;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.helpers.CookiesHelper;
import com.techstore.app.logger.CartAuditLogger;
import com.techstore.app.mapper.CartItemMapper;
import com.techstore.app.mapper.CartProductMapper;
import com.techstore.app.repository.CartRepository;
import com.techstore.app.repository.CustomerRepository;
import com.techstore.app.repository.ProductRepository;
import com.techstore.app.service.interfaces.CartService;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CartItemServiceImpl Tests")
class CartItemServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CartAuditLogger cartAuditLogger;

    @Mock
    private HttpServletRequest request;

    @Mock
    private FileUploadConfig fileUploadConfig;

    @InjectMocks
    private CartItemServiceImpl cartItemService;

    private SupabaseUserId supabaseUserId;
    private Customer customer;
    private User user;
    private Product product;
    private ProductId productId;
    private Cart cart;
    private CartId cartId;
    private CartItemDto cartItemDto;
    private UpdateCartItemDto updateCartItemDto;
    private Email email;

    @BeforeEach
    void setUp() {
        supabaseUserId = SupabaseUserId.newId();
        productId = ProductId.newId();
        cartId = CartId.newId();

        email = mock(Email.class);
        when(email.getEmail()).thenReturn("test@example.com");

        UserId userId = UserId.newId();
        user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn(email);

        customer = mock(Customer.class);
        when(customer.getUser()).thenReturn(user);

        product = mock(Product.class);
        when(product.getId()).thenReturn(productId);
        
        ProductName productName = mock(ProductName.class);
        when(productName.getProductName()).thenReturn("Test Product");
        when(product.getName()).thenReturn(productName);
        
        ProductDescription productDescription = mock(ProductDescription.class);
        when(productDescription.getDescription()).thenReturn("Test Description");
        when(product.getDescription()).thenReturn(productDescription);
        
        Money money = mock(Money.class);
        when(money.getMoneyValue()).thenReturn(java.math.BigDecimal.valueOf(99.99));
        when(product.getPrice()).thenReturn(money);

        cart = new Cart(customer);
        
        cartItemDto = new CartItemDto(productId.getId(), 2);
        updateCartItemDto = new UpdateCartItemDto(1);
    }

    @Test
    @DisplayName("Should add item to existing cart successfully")
    void testAddItemToCartSuccess() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(productRepository.findById_Id(productId.getId())).thenReturn(product);
            when(cartRepository.findCartByCustomerEmail("test@example.com")).thenReturn(cart);

            cartItemService.addItemToCart(cartItemDto, request);

            verify(cartService, times(1)).addNewItem(any(CartItem.class), eq(cart));
        }
    }

    @Test
    @DisplayName("Should create new cart when cart does not exist")
    void testAddItemToCartCreateNewCart() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(productRepository.findById_Id(productId.getId())).thenReturn(product);
            when(cartRepository.findCartByCustomerEmail("test@example.com")).thenReturn(null);
            when(cartService.createCart(customer)).thenReturn(cart);

            cartItemService.addItemToCart(cartItemDto, request);

            verify(cartService, times(1)).createCart(customer);
            verify(cartService, times(1)).addNewItem(any(CartItem.class), eq(cart));
            verify(cartAuditLogger, times(1)).logCartCreation(
                    customer.getUser().getId().toString(),
                    cart.getId().toString());
        }
    }

    @Test
    @DisplayName("Should throw exception when user not found on add item")
    void testAddItemToCartUserNotFound() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, 
                    () -> cartItemService.addItemToCart(cartItemDto, request));

            verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                    eq("UNKNOWN"),
                    anyString(),
                    eq("User not found"));
        }
    }

    @Test
    @DisplayName("Should throw exception when product not found on add item")
    void testAddItemToCartProductNotFound() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(productRepository.findById_Id(productId.getId())).thenReturn(null);

            assertThrows(BusinessException.class, 
                    () -> cartItemService.addItemToCart(cartItemDto, request));

            verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                    eq("N/A"),
                    anyString(),
                    eq("Product not found"));
        }
    }

    @Test
    @DisplayName("Should update item in cart successfully")
    void testUpdateItemInCartSuccess() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartRepository.findCartByCustomerEmail("test@example.com")).thenReturn(cart);

            cartItemService.updateItemInCart(productId.getId().toString(), updateCartItemDto, request);

            verify(cartService, times(1)).updateItem(productId, 1, cart.getId());
        }
    }

    @Test
    @DisplayName("Should throw exception when user not found on update item")
    void testUpdateItemInCartUserNotFound() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, 
                    () -> cartItemService.updateItemInCart(productId.getId().toString(), updateCartItemDto, request));

            verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                    "UNKNOWN",
                    productId.getId().toString(),
                    "User not found");
        }
    }

    @Test
    @DisplayName("Should throw exception when product not found on update item")
    void testUpdateItemInCartProductNotFound() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class, 
                    () -> cartItemService.updateItemInCart(productId.getId().toString(), updateCartItemDto, request));

            verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                    "UNKNOWN",
                    productId.getId().toString(),
                    "Product not found");
        }
    }

    @Test
    @DisplayName("Should throw exception when cart not found on update item")
    void testUpdateItemInCartCartNotFound() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartRepository.findCartByCustomerEmail("test@example.com")).thenReturn(null);

            assertThrows(BusinessException.class, 
                    () -> cartItemService.updateItemInCart(productId.getId().toString(), updateCartItemDto, request));

            verify(cartAuditLogger, times(2)).logCartUpdateFailure(
                    eq("UNKNOWN"),
                    eq(productId.getId().toString()),
                    eq("Cart not found"));
        }
    }

    @Test
    @DisplayName("Should remove item from cart successfully")
    void testRemoveItemFromCartSuccess() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartRepository.findCartByCustomerEmail("test@example.com")).thenReturn(cart);

            cartItemService.removeItemFromCart(productId.getId().toString(), request);

            verify(cartService, times(1)).removeItem(productId, cart.getId());
        }
    }

    @Test
    @DisplayName("Should throw exception when user not found on remove item")
    void testRemoveItemFromCartUserNotFound() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, 
                    () -> cartItemService.removeItemFromCart(productId.getId().toString(), request));

            verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                    "UNKNOWN",
                    productId.getId().toString(),
                    "User not found");
        }
    }

    @Test
    @DisplayName("Should throw exception when product not found on remove item")
    void testRemoveItemFromCartProductNotFound() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class, 
                    () -> cartItemService.removeItemFromCart(productId.getId().toString(), request));

            verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                    "UNKNOWN",
                    productId.getId().toString(),
                    "Product not found");
        }
    }

    @Test
    @DisplayName("Should throw exception when cart not found on remove item")
    void testRemoveItemFromCartCartNotFound() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartRepository.findCartByCustomerEmail("test@example.com")).thenReturn(null);

            assertThrows(BusinessException.class, 
                    () -> cartItemService.removeItemFromCart(productId.getId().toString(), request));

            verify(cartAuditLogger, times(2)).logCartUpdateFailure(
                    eq("UNKNOWN"),
                    eq(productId.getId().toString()),
                    eq("Cart not found"));
        }
    }

    @Test
    @DisplayName("Should retrieve all cart items successfully")
    void testGetAllCartItemsSuccess() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            CartItem cartItem = new CartItem(2, product);
            cart.addItem(cartItem);

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(cartRepository.findCartByCustomerEmail("test@example.com")).thenReturn(cart);
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            List<CartProductResponseDto> result = cartItemService.getAllCartItems(request);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(cartAuditLogger, times(1)).logCartRetrieved(cart.getId().toString(), 1);
        }
    }

    @Test
    @DisplayName("Should return empty list when cart has no items")
    void testGetAllCartItemsEmpty() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            cart = new Cart(customer);

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(cartRepository.findCartByCustomerEmail("test@example.com")).thenReturn(cart);

            List<CartProductResponseDto> result = cartItemService.getAllCartItems(request);

            assertNotNull(result);
            assertEquals(0, result.size());
            verify(cartAuditLogger, times(1)).logCartRetrieved(cart.getId().toString(), 0);
        }
    }

    @Test
    @DisplayName("Should throw exception when user not found on get all items")
    void testGetAllCartItemsUserNotFound() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, 
                    () -> cartItemService.getAllCartItems(request));

            verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                    "UNKNOWN",
                    "MULTIPLE",
                    "User not found");
        }
    }

    @Test
    @DisplayName("Should throw exception when cart not found on get all items")
    void testGetAllCartItemsCartNotFound() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(cartRepository.findCartByCustomerEmail("test@example.com")).thenReturn(null);

            assertThrows(BusinessException.class, 
                    () -> cartItemService.getAllCartItems(request));

            verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                    "UNKNOWN",
                    "MULTIPLE",
                    "Cart not found");
        }
    }

    @Test
    @DisplayName("Should throw exception when product not found in cart on get all items")
    void testGetAllCartItemsProductNotFound() {
        try (MockedStatic<CookiesHelper> mockedHelper = mockStatic(CookiesHelper.class)) {
            mockedHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId.getId().toString());

            CartItem cartItem = new CartItem(2, product);
            cart.addItem(cartItem);

            when(customerRepository.findBySupabaseUserId(supabaseUserId))
                    .thenReturn(Optional.of(customer));
            when(cartRepository.findCartByCustomerEmail("test@example.com")).thenReturn(cart);
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class, 
                    () -> cartItemService.getAllCartItems(request));

            verify(cartAuditLogger, times(1)).logCartUpdateFailure(
                    "UNKNOWN",
                    "MULTIPLE",
                    "Product not found for item in cart");
        }
    }
}