package com.granishots.inventory.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SupplyJpaRepository extends JpaRepository<SupplyData, Long> {
    List<SupplyData> findByNameContainingIgnoreCase(String name);

    @Query("SELECT s FROM SupplyData s WHERE s.currentStock <= s.minStock AND s.currentStock > 0")
    List<SupplyData> findLowStock();

    @Query("SELECT s FROM SupplyData s WHERE s.currentStock = 0")
    List<SupplyData> findOutOfStock();
}
