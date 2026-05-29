package com.granishots.inventory.domain.model.gateway;

import com.granishots.inventory.domain.model.Supply;
import java.util.List;

public interface SupplyGateway {
    Supply save(Supply supply);
    Supply findById(Long id);
    List<Supply> findAll();
    List<Supply> findLowStock();
    List<Supply> findOutOfStock();
    List<Supply> findByName(String name);
    Supply update(Supply supply);
    void deleteById(Long id);
    Supply updateMinStock(Long id, Double minStock);
    Supply addStock(Long id, Double quantity);
    Supply subtractStock(Long id, Double quantity);
}
