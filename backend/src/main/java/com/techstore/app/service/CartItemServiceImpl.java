package com.techstore.app.service;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartItem;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.product.ProductId;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.dto.cart.CartItemDto;
import com.techstore.app.dto.cart.CartProductResponseDto;
import com.techstore.app.dto.cart.UpdateCartItemDto;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.helpers.CookiesHelper;
import com.techstore.app.logger.CartAuditLogger;
import com.techstore.app.mapper.CartItemMapper;
import com.techstore.app.mapper.CartProductMapper;
import com.techstore.app.repository.*;
import com.techstore.app.service.interfaces.CartItemService;
import com.techstore.app.service.interfaces.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

        private final CartRepository cartRepository;
        private final ProductRepository productRepository;
        private final CartService cartService;
        private final CustomerRepository customerRepository;
        private final CartAuditLogger cartAuditLogger;

        @Override
        public void addItemToCart(CartItemDto cartItemDto, HttpServletRequest request) {

                try {
                        SupabaseUserId supabaseUserId = SupabaseUserId.fromString(CookiesHelper.getCurrentUserId());

                        Customer customer = customerRepository.findBySupabaseUserId(supabaseUserId)
                                        .orElseThrow(() -> new BusinessException("User not found"));

                        Product product = productRepository.findById_Id(cartItemDto.productId());

                        if (product == null) {
                                cartAuditLogger.logCartUpdateFailure(
                                                "N/A",
                                                String.valueOf(cartItemDto.productId()),
                                                "Product not found");
                                throw new BusinessException("Product not found");
                        }

                        CartItem cartItem = CartItemMapper.toEntity(cartItemDto.quantity(), product);

                        Cart cart = cartRepository.findCartByCustomerEmail(

                                        customer.getUser().getEmail().getEmail()

                        );

                        if (cart == null) {

                                cart = cartService.createCart(customer);

                                cartAuditLogger.logCartCreation(
                                                customer.getUser().getId().toString(),
                                                cart.getId().toString());
                        }

                        cartService.addNewItem(cartItem, cart);
                } catch (BusinessException e) {
                        cartAuditLogger.logCartUpdateFailure(
                                        "UNKNOWN",
                                        cartItemDto.productId().toString(),
                                        e.getMessage());
                        throw e;
                }
        }

        @Override
        public void updateItemInCart(String productIdStr, UpdateCartItemDto updateCartItemDto,
                        HttpServletRequest request) {
                try {
                        SupabaseUserId supabaseUserId = SupabaseUserId.fromString(CookiesHelper.getCurrentUserId());
                        Customer customer = customerRepository.findBySupabaseUserId(supabaseUserId)
                                        .orElseThrow(() -> new BusinessException("User not found"));

                        ProductId productId = ProductId.fromString(productIdStr);

                        Product product = productRepository.findById(productId)
                                        .orElseThrow(() -> new BusinessException("Product not found"));

                        Cart cart = cartRepository.findCartByCustomerEmail(
                                        customer.getUser().getEmail().getEmail());

                        if (cart == null) {

                                cartAuditLogger.logCartUpdateFailure(
                                                "UNKNOWN",
                                                productIdStr,
                                                "Cart not found");
                                throw new BusinessException("Cart not found");
                        }

                        cartService.updateItem(productId, updateCartItemDto.quantityDelta(), cart.getId());

                } catch (BusinessException e) {

                        cartAuditLogger.logCartUpdateFailure(
                                        "UNKNOWN",
                                        productIdStr,
                                        e.getMessage());
                        throw e;
                }
        }

        @Override
        public void removeItemFromCart(String productIdStr, HttpServletRequest request) {
                try {
                        SupabaseUserId supabaseUserId = SupabaseUserId.fromString(CookiesHelper.getCurrentUserId());

                        Customer customer = customerRepository.findBySupabaseUserId(supabaseUserId)
                                        .orElseThrow(() -> new BusinessException("User not found"));

                        ProductId productId = ProductId.fromString(productIdStr);

                        Product product = productRepository.findById(productId)
                                        .orElseThrow(() -> new BusinessException("Product not found"));

                        Cart cart = cartRepository.findCartByCustomerEmail(
                                        customer.getUser().getEmail().getEmail());

                        if (cart == null) {
                                cartAuditLogger.logCartUpdateFailure(
                                                "UNKNOWN",
                                                productIdStr,
                                                "Cart not found");
                                throw new BusinessException("Cart not found");
                        }

                        cartService.removeItem(productId, cart.getId());
                } catch (BusinessException e) {

                        cartAuditLogger.logCartUpdateFailure(
                                        "UNKNOWN",
                                        productIdStr,
                                        e.getMessage());
                        throw e;
                }
        }

        @Override
    public List<CartProductResponseDto> getAllCartItems(HttpServletRequest request) {
        try {
            SupabaseUserId supabaseUserId = SupabaseUserId.fromString(CookiesHelper.getCurrentUserId());
 
            Customer customer = customerRepository.findBySupabaseUserId(supabaseUserId)
                    .orElseThrow(() -> new BusinessException("User not found"));
 
            Cart cart = cartRepository.findCartByCustomerEmail(
                    customer.getUser().getEmail().getEmail()
            );
 
            if (cart == null) {
                throw new BusinessException("Cart not found");
            }
 
            List<CartProductResponseDto> response = new ArrayList<>();
 
            for (CartItem cartItem : cart.getItems()) {
                ProductId productId = cartItem.getProduct().getId();
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new BusinessException("Product not found for item in cart"));
 
                CartProductResponseDto dto = CartProductMapper.toResponseDto(cartItem, product);
                response.add(dto);
            }
 
            cartAuditLogger.logCartRetrieved(
                    cart.getId().toString(),
                    response.size()
            );
 
            return response;
 
        } catch (BusinessException e) {
            cartAuditLogger.logCartUpdateFailure(
                    "UNKNOWN",
                    "MULTIPLE",
                    e.getMessage()
            );
            throw e;
        }
    }

}
