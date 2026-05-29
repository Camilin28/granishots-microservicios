package com.granishots.orders.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderItemRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String productName;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer quantity;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    private Double unitPrice;

    private String size;
    private String flavor;
    private String toppings;
    private String notes;
}
