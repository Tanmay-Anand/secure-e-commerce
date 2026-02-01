package com.tanmay.secure_e_commerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private Long id;

    @NotNull
    private Long productId;

    private String productName;

    @NotNull
    @Min(value = 1)
    private Integer quantity;

    private BigDecimal price;
}
