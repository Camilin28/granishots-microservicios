package com.granishots.catalog.domain.model.gateway;

import com.granishots.catalog.domain.model.Product;
import java.util.List;

public interface ProductGateway {
    Product save(Product product);
    Product findById(Long id);
    List<Product> findAll();
    List<Product> findByCategory(String category);
    List<Product> findBySize(String size);
    List<Product> findAvailable();
    List<Product> findByName(String name);
    Product update(Product product);
    void deleteById(Long id);
    Product updateStatus(Long id, Boolean available);
    Product updatePrice(Long id, Double price);
}
