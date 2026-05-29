package com.granishots.catalog.infraestructure.driver_adapters.jpa_repository;

import com.granishots.catalog.domain.model.Product;
import com.granishots.catalog.domain.model.gateway.ProductGateway;
import com.granishots.catalog.infraestructure.mapper.ProductMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@AllArgsConstructor
public class ProductGatewayImp implements ProductGateway {

    private final ProductJpaRepository repo;
    private final ProductMapper mapper;

    @Override
    public Product save(Product p) {
        return mapper.toProduct(repo.save(mapper.toProductData(p)));
    }

    @Override
    public Product findById(Long id) {
        return repo.findById(id).map(mapper::toProduct).orElse(null);
    }

    @Override
    public List<Product> findAll() {
        return repo.findAll().stream().map(mapper::toProduct).toList();
    }

    @Override
    public List<Product> findByCategory(String category) {
        return repo.findByCategory(category).stream().map(mapper::toProduct).toList();
    }

    @Override
    public List<Product> findBySize(String size) {
        return repo.findBySize(size).stream().map(mapper::toProduct).toList();
    }

    @Override
    public List<Product> findAvailable() {
        return repo.findByAvailableTrue().stream().map(mapper::toProduct).toList();
    }

    @Override
    public List<Product> findByName(String name) {
        return repo.findByNameContainingIgnoreCase(name).stream().map(mapper::toProduct).toList();
    }

    @Override
    public Product update(Product p) {
        return mapper.toProduct(repo.save(mapper.toProductData(p)));
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Product updateStatus(Long id, Boolean available) {
        ProductData d = repo.findById(id).orElseThrow();
        d.setAvailable(available);
        return mapper.toProduct(repo.save(d));
    }

    @Override
    public Product updatePrice(Long id, Double price) {
        ProductData d = repo.findById(id).orElseThrow();
        d.setPrice(price);
        return mapper.toProduct(repo.save(d));
    }
}
