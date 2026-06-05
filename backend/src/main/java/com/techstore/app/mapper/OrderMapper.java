package com.techstore.app.mapper;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderItem;
import com.techstore.app.domain.order.OrderStatus;
import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.dto.shared.AddAddressDTO;
import com.techstore.app.dto.shared.AddressDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {

    public static Order toEntity(CreateOrderRequestDTO dto, List<OrderItem> orderItems, BigDecimal total) {
        AddAddressDTO address = dto.address();

        return new Order(total, address.postalCode(), address.city(),
                address.country(), address.street(), OrderStatus.PENDING, orderItems);
    }

    public static OrderResponseDTO toResponse(Order order, Customer customer, Cart cart) {
        AddressDTO addressDTO = new AddressDTO(order.getAddress().getPostalCode(), order.getAddress().getCity(),
                order.getAddress().getCountry(), order.getAddress().getStreet());

        return new OrderResponseDTO(order.getId().getId().toString(), customer.getId().getId().toString(),
                cart.getId().getId().toString(), addressDTO);
    }

    public static OrderResponseDTO toResponse(Order order, String cartID) {
        AddressDTO addressDTO = new AddressDTO(order.getAddress().getPostalCode(),
                order.getAddress().getCity(), order.getAddress().getCountry(),
                order.getAddress().getStreet());

        return new OrderResponseDTO(order.getId().getId().toString(),
                order.getCustomer().getId().getId().toString(), cartID, addressDTO);
    }
}