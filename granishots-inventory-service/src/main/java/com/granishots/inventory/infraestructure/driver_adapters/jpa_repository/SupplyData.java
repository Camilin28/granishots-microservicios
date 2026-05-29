package com.granishots.inventory.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "supplies")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplyData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "current_stock", nullable = false)
    private Double currentStock;

    @Column(name = "min_stock", nullable = false)
    private Double minStock;

    @Column(length = 150)
    private String supplier;

    @Column(name = "purchase_price")
    private Double purchasePrice;
}
