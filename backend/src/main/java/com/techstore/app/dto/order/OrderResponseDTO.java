package com.techstore.app.dto.order;

import com.techstore.app.dto.shared.AddressDTO;

public record OrderResponseDTO(String orderID, String customerID, String cartID, AddressDTO address) {
}
