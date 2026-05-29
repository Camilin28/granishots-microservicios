package com.granishots.catalog.infraestructure.entry_points;

import com.granishots.catalog.application.dto.ApiResponse;
import com.granishots.catalog.application.dto.ProductRequestDTO;
import com.granishots.catalog.application.dto.ProductResponseDTO;
import com.granishots.catalog.domain.exception.BusinessException;
import com.granishots.catalog.domain.model.Product;
import com.granishots.catalog.domain.usecase.ProductUseCase;
import com.granishots.catalog.infraestructure.mapper.ProductMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCase productUseCase;
    private final ProductMapper productMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDTO>> save(@Valid @RequestBody ProductRequestDTO dto) {
        Product product = productUseCase.save(productMapper.toProductFromDTO(dto));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Producto registrado exitosamente", productMapper.toProductResponseDTO(product)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> findAll() {
        List<ProductResponseDTO> list = productUseCase.findAll().stream().map(productMapper::toProductResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Productos obtenidos — total: " + list.size(), list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Producto encontrado",
                productMapper.toProductResponseDTO(productUseCase.findById(id))));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> findByCategory(@PathVariable String category) {
        List<ProductResponseDTO> list = productUseCase.findByCategory(category).stream()
                .map(productMapper::toProductResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Productos categoría '" + category.toUpperCase() + "' — total: " + list.size(), list));
    }

    @GetMapping("/size/{size}")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> findBySize(@PathVariable String size) {
        List<ProductResponseDTO> list = productUseCase.findBySize(size).stream()
                .map(productMapper::toProductResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Productos tamaño " + size + " oz — total: " + list.size(), list));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> findAvailable() {
        List<ProductResponseDTO> list = productUseCase.findAvailable().stream()
                .map(productMapper::toProductResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Productos disponibles — total: " + list.size(), list));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> findByName(@RequestParam String name) {
        List<ProductResponseDTO> list = productUseCase.findByName(name).stream()
                .map(productMapper::toProductResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Resultados para '" + name + "' — total: " + list.size(), list));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> update(
            @PathVariable Long id, @Valid @RequestBody ProductRequestDTO dto) {
        Product updated = productUseCase.update(id, productMapper.toProductFromDTO(dto));
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado exitosamente",
                productMapper.toProductResponseDTO(updated)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateStatus(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!body.containsKey("available"))
            throw new BusinessException("El campo 'available' es obligatorio", 400);
        Object val = body.get("available");
        if (!(val instanceof Boolean))
            throw new BusinessException("El campo 'available' debe ser true o false", 400);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado",
                productMapper.toProductResponseDTO(productUseCase.updateStatus(id, (Boolean) val))));
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updatePrice(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!body.containsKey("price"))
            throw new BusinessException("El campo 'price' es obligatorio", 400);
        Double price;
        try { price = Double.parseDouble(body.get("price").toString()); }
        catch (NumberFormatException e) { throw new BusinessException("El campo 'price' debe ser un número válido", 400); }
        return ResponseEntity.ok(ApiResponse.ok("Precio actualizado",
                productMapper.toProductResponseDTO(productUseCase.updatePrice(id, price))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        productUseCase.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Producto " + id + " eliminado correctamente", null));
    }
}
