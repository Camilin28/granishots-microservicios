package com.granishots.catalog.domain.usecase;

import com.granishots.catalog.domain.exception.BusinessException;
import com.granishots.catalog.domain.model.Product;
import com.granishots.catalog.domain.model.gateway.ProductGateway;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class ProductUseCase {

    private final ProductGateway productGateway;

    private static final double PRECIO_MAXIMO = 999_999;
    private static final int NOMBRE_MAX = 100;
    private static final int DESC_MAX = 500;
    private static final List<String> CATEGORIAS = List.of("PEQUEÑA", "MEDIANO", "MEGA");
    private static final List<String> SIZES = List.of("8", "16", "24");

    public Product save(Product product) {
        validarProducto(product);
        boolean duplicado = productGateway.findByName(product.getName().trim())
                .stream().anyMatch(p -> p.getName().equalsIgnoreCase(product.getName().trim()));
        if (duplicado)
            throw new BusinessException("Ya existe un producto con el nombre '" + product.getName().trim() + "'", 409);
        product.setName(product.getName().trim());
        if (product.getDescription() != null) product.setDescription(product.getDescription().trim());
        product.setCategory(product.getCategory().trim().toUpperCase());
        product.setSize(product.getSize().trim());
        product.setAvailable(true);
        return productGateway.save(product);
    }

    public Product findById(Long id) {
        validarId(id);
        Product p = productGateway.findById(id);
        if (p == null) throw new BusinessException("No existe un producto con el id: " + id, 404);
        return p;
    }

    public List<Product> findAll() { return productGateway.findAll(); }

    public List<Product> findByCategory(String category) {
        if (category == null || category.trim().isEmpty())
            throw new BusinessException("La categoría no puede estar vacía", 400);
        String cat = category.trim().toUpperCase();
        if (!CATEGORIAS.contains(cat))
            throw new BusinessException("Categoría inválida: '" + category + "'. Válidas: " + CATEGORIAS, 400);
        return productGateway.findByCategory(cat);
    }

    public List<Product> findBySize(String size) {
        if (size == null || size.trim().isEmpty())
            throw new BusinessException("El tamaño no puede estar vacío", 400);
        if (!SIZES.contains(size.trim()))
            throw new BusinessException("Tamaño inválido: '" + size + "'. Válidos: " + SIZES + " (onzas)", 400);
        return productGateway.findBySize(size.trim());
    }

    public List<Product> findAvailable() { return productGateway.findAvailable(); }

    public List<Product> findByName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new BusinessException("El término de búsqueda no puede estar vacío", 400);
        if (name.trim().length() < 2)
            throw new BusinessException("El término debe tener al menos 2 caracteres", 400);
        return productGateway.findByName(name.trim());
    }

    public Product update(Long id, Product product) {
        validarId(id);
        validarProducto(product);
        Product existente = productGateway.findById(id);
        if (existente == null) throw new BusinessException("No existe un producto con el id: " + id, 404);
        boolean duplicado = productGateway.findByName(product.getName().trim())
                .stream().anyMatch(p -> p.getName().equalsIgnoreCase(product.getName().trim()) && !p.getId().equals(id));
        if (duplicado)
            throw new BusinessException("Ya existe otro producto con el nombre '" + product.getName().trim() + "'", 409);
        product.setId(id);
        product.setName(product.getName().trim());
        if (product.getDescription() != null) product.setDescription(product.getDescription().trim());
        product.setCategory(product.getCategory().trim().toUpperCase());
        product.setSize(product.getSize().trim());
        product.setAvailable(existente.getAvailable());
        return productGateway.update(product);
    }

    public void deleteById(Long id) {
        validarId(id);
        if (productGateway.findById(id) == null)
            throw new BusinessException("No existe un producto con el id: " + id, 404);
        productGateway.deleteById(id);
    }

    public Product updateStatus(Long id, Boolean available) {
        validarId(id);
        if (available == null) throw new BusinessException("El campo 'available' no puede ser nulo", 400);
        Product p = productGateway.findById(id);
        if (p == null) throw new BusinessException("No existe un producto con el id: " + id, 404);
        if (p.getAvailable().equals(available))
            throw new BusinessException("El producto ya está " + (available ? "activo" : "inactivo"), 400);
        return productGateway.updateStatus(id, available);
    }

    public Product updatePrice(Long id, Double price) {
        validarId(id);
        if (price == null) throw new BusinessException("El precio no puede ser nulo", 400);
        if (price <= 0) throw new BusinessException("El precio debe ser mayor a cero. Recibido: " + price, 400);
        if (price > PRECIO_MAXIMO) throw new BusinessException("El precio no puede superar " + PRECIO_MAXIMO, 400);
        if (productGateway.findById(id) == null)
            throw new BusinessException("No existe un producto con el id: " + id, 404);
        return productGateway.updatePrice(id, price);
    }

    private void validarId(Long id) {
        if (id == null) throw new BusinessException("El ID no puede ser nulo", 400);
        if (id <= 0) throw new BusinessException("El ID debe ser positivo. Recibido: " + id, 400);
    }

    private void validarProducto(Product p) {
        if (p == null) throw new BusinessException("El cuerpo del producto no puede ser nulo", 400);
        if (p.getName() == null || p.getName().trim().isEmpty())
            throw new BusinessException("El nombre no puede estar vacío", 400);
        if (p.getName().trim().length() > NOMBRE_MAX)
            throw new BusinessException("El nombre no puede superar " + NOMBRE_MAX + " caracteres", 400);
        if (p.getDescription() != null && p.getDescription().trim().length() > DESC_MAX)
            throw new BusinessException("La descripción no puede superar " + DESC_MAX + " caracteres", 400);
        if (p.getPrice() == null) throw new BusinessException("El precio es obligatorio", 400);
        if (p.getPrice() <= 0) throw new BusinessException("El precio debe ser mayor a cero. Recibido: " + p.getPrice(), 400);
        if (p.getPrice() > PRECIO_MAXIMO) throw new BusinessException("El precio no puede superar " + PRECIO_MAXIMO, 400);
        if (p.getCategory() == null || p.getCategory().trim().isEmpty())
            throw new BusinessException("La categoría no puede estar vacía", 400);
        if (!CATEGORIAS.contains(p.getCategory().trim().toUpperCase()))
            throw new BusinessException("Categoría inválida: '" + p.getCategory() + "'. Válidas: " + CATEGORIAS, 400);
        if (p.getSize() == null || p.getSize().trim().isEmpty())
            throw new BusinessException("El tamaño es obligatorio", 400);
        if (!SIZES.contains(p.getSize().trim()))
            throw new BusinessException("Tamaño inválido: '" + p.getSize() + "'. Válidos: " + SIZES + " (onzas)", 400);
    }
}
