package com.techstore.app.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartProductResponseDto implements Serializable {
    
    private String productId;
    private String productName;
    private String productDescription;
    private Integer quantity;
    private BigDecimal price;
}