package com.granishots.catalog.infraestructure.mapper;

import com.granishots.catalog.application.dto.ProductRequestDTO;
import com.granishots.catalog.application.dto.ProductResponseDTO;
import com.granishots.catalog.domain.model.Product;
import com.granishots.catalog.infraestructure.driver_adapters.jpa_repository.ProductData;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductData toProductData(Product product) {
        return new ProductData(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getSize(),
                product.getAvailable()
        );
    }

    public Product toProduct(ProductData data) {
        return new Product(
                data.getId(),
                data.getName(),
                data.getDescription(),
                data.getPrice(),
                data.getCategory(),
                data.getSize(),
                data.getAvailable()
        );
    }

    public Product toProductFromDTO(ProductRequestDTO dto) {
        return new Product(
                null,
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getCategory(),
                dto.getSize(),
                true
        );
    }

    public ProductResponseDTO toProductResponseDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getSize(),
                product.getAvailable()
        );
    }
}
