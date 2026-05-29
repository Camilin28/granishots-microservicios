package com.granishots.orders.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private String customerName;
    private String customerPhone;
    private String status;   // PENDING, CONFIRMED, IN_PREPARATION, READY, DELIVERED, CANCELLED
    private String channel;  // LOCAL, DELIVERY, PHONE
    private Integer tableNumber;
    private String notes;
    private Double total;
    private LocalDateTime createdAt;
    private List<OrderItem> items;
}
