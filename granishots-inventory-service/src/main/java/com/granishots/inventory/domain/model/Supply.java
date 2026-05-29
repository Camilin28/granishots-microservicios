package com.granishots.inventory.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Supply {
    private Long id;
    private String name;
    private String unit;
    private Double currentStock;
    private Double minStock;
    private String supplier;
    private Double purchasePrice;
}
