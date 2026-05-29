package com.granishots.inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplyResponseDTO {
    private Long id;
    private String name;
    private String unit;
    private Double currentStock;
    private Double minStock;
    private String supplier;
    private Double purchasePrice;
    private String stockStatus;
}
