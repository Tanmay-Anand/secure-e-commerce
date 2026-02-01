package com.tanmay.secure_e_commerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name required")
    private String name;


    private String description;

    @NotNull(message = "Price Required")
    @Min(value = 0, message = "Price must be Positive")
    private BigDecimal price;

    @NotNull(message = "Stock Required")
    @Min(value = 0)
    private Integer stock;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String categoryName;
}
