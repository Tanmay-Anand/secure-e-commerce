package com.tanmay.secure_e_commerce.dto;

import com.tanmay.secure_e_commerce.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long id;
    private Long userId;
    private String username;

    @NotEmpty(message = "at least 1 item")
    @Valid
    private List<OrderItemDTO> orderItems;

    private BigDecimal totalAmount;
    private OrderStatus status;
}
