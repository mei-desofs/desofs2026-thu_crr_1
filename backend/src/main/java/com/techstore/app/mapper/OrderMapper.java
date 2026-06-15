package com.techstore.app.mapper;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderItem;
import com.techstore.app.domain.order.OrderStatus;
import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.ManagerOrderResponseDTO;
import com.techstore.app.dto.order.OrderItemDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.dto.order.OrderSummaryDTO;
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

        public static OrderSummaryDTO toSummary(Order order, String uploadBasePath) {

                return new OrderSummaryDTO(
                                order.getId() != null ? order.getId().getId().toString() : null,
                                order.getCustomer() != null && order.getCustomer().getId() != null
                                                ? order.getCustomer().getId().getId().toString()
                                                : null,
                                order.getCarrier() != null && order.getCarrier().getId() != null
                                                ? order.getCarrier().getId().getId().toString()
                                                : null,
                                order.getOrderStatus(),
                                order.getTotalPrice() != null ? order.getTotalPrice().getMoneyValue() : null,
                                order.getAddress() != null ? new AddressDTO(
                                                order.getAddress().getPostalCode(),
                                                order.getAddress().getCity(),
                                                order.getAddress().getCountry(),
                                                order.getAddress().getStreet()) : null,
                                mapItems(order.getOrderItems(), uploadBasePath));
        }

        private static List<OrderItemDTO> mapItems(List<OrderItem> items, String uploadBasePath) {
                if (items == null)
                        return List.of();

                return items.stream()
                                .map(i -> new OrderItemDTO(
                                                i.getProduct() != null && i.getProduct().getId() != null
                                                                ? i.getProduct().getId().getId().toString()
                                                                : null,
                                                i.getProduct() != null && i.getProduct().getName() != null
                                                                ? i.getProduct().getName().getProductName()
                                                                : null,
                                                i.getQuantity() != null ? i.getQuantity().getQuantity() : null,
                                                i.getPrice() != null ? i.getPrice().getMoneyValue() : null,
                                                ProductImageDataUrlMapper.toDataUrl(
                                                                i.getProduct() != null ? i.getProduct().getImagePath()
                                                                                : null,
                                                                uploadBasePath)))
                                .toList();
        }

        public static ManagerOrderResponseDTO toManagerResponse(Order order) {
        return new ManagerOrderResponseDTO(    
             order.getId().getId(),
            order.getCustomer().getId().getId(),
            order.getCustomer().getUser().getEmail().getEmail(),
            order.getTotalPrice(),
            order.getOrderStatus().toString(),
            order.getCreatedAt(),
            order.getOrderItems().size()
        );
    }
}