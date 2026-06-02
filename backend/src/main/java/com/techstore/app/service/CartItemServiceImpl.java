package com.techstore.app.service;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartItem;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.dto.cart.CartItemDto;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.helpers.CookiesHelper;
import com.techstore.app.logger.CartAuditLogger;
import com.techstore.app.mapper.CartItemMapper;
import com.techstore.app.repository.*;
import com.techstore.app.service.interfaces.CartItemService;
import com.techstore.app.service.interfaces.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final CustomerRepository customerRepository;
    private final CartAuditLogger cartAuditLogger;

    @Override
    public void addItemToCart(CartItemDto cartItemDto, HttpServletRequest request) {
        SupabaseUserId supabaseUserId = SupabaseUserId.fromString(CookiesHelper.getCurrentUserId());

        Customer customer = customerRepository.findBySupabaseUserId(supabaseUserId)
                .orElseThrow(() -> new BusinessException("User not found"));

        Product product = productRepository.findById_Id(cartItemDto.productId());

        if (product == null) {
            cartAuditLogger.logCartUpdateFailure(
                    "N/A",
                    String.valueOf(cartItemDto.productId()),
                    "Product not found"
            );
            throw new BusinessException("Product not found");
        }

        CartItem cartItem =
                CartItemMapper.toEntity(cartItemDto.quantity(), product);

        Cart cart = cartRepository.findCartByCustomerEmail(
                customer.getUser().getEmail().getEmail()
        );

        if (cart == null) {
            cartService.createCart(cartItem, customer);

            cartAuditLogger.logCartCreation(
                    customer.getUser().getId().toString(),
                    "NEW_CART"
            );
            return;
        }

        cartService.addNewItem(cartItem, cart.getId());
    }
}
