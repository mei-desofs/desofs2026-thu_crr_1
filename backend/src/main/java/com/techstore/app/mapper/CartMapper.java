package com.techstore.app.mapper;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.dto.cart.CartProductDTO;
import com.techstore.app.dto.cart.CartReponseDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public static Cart toEntity(Customer customer) {
        return new Cart(
            customer
        );
    }

    public static CartReponseDTO toCartItemsResponseDTO(Cart cart) {
        Map<UUID, CartProductDTO> products = cart.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getId().getId(),
                        item -> new CartProductDTO(
                                item.getProduct().getName().getProductName(),
                                item.getQuantity().getQuantity(),
                                item.getProduct().getPrice().getMoneyValue()
                        )
                ));

        return new CartReponseDTO(products);
    }
}
