package com.granishots.orders.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String customerName;

    @Pattern(regexp = "^[0-9]{7,10}$", message = "El teléfono debe tener entre 7 y 10 dígitos")
    private String customerPhone;

    @NotBlank(message = "El canal es obligatorio")
    @Pattern(regexp = "^(LOCAL|DELIVERY|PHONE)$", message = "El canal debe ser LOCAL, DELIVERY o PHONE")
    private String channel;

    private Integer tableNumber;
    private String notes;

    @NotNull(message = "Los ítems del pedido son obligatorios")
    @Size(min = 1, message = "El pedido debe tener al menos un ítem")
    @Valid
    private List<OrderItemRequestDTO> items;
}
