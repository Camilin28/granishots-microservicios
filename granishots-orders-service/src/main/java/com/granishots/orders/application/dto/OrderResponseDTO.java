package com.granishots.orders.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private String customerName;
    private String customerPhone;
    private String status;
    private String channel;
    private Integer tableNumber;
    private String notes;
    private Double total;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDTO> items;
}
