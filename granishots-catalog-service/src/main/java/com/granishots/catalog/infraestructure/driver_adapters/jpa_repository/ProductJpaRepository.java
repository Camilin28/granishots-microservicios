package com.granishots.catalog.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductData, Long> {
    List<ProductData> findByCategory(String category);
    List<ProductData> findBySize(String size);
    List<ProductData> findByAvailableTrue();
    List<ProductData> findByNameContainingIgnoreCase(String name);
}
