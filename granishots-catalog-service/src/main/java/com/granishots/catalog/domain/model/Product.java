package com.granishots.catalog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;   // PEQUEÑA, MEDIANO, MEGA
    private String size;       // 8, 16, 24
    private Boolean available;
}
