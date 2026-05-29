package com.granishots.inventory.infraestructure.entry_points;

import com.granishots.inventory.application.dto.ApiResponse;
import com.granishots.inventory.application.dto.SupplyRequestDTO;
import com.granishots.inventory.application.dto.SupplyResponseDTO;
import com.granishots.inventory.domain.exception.BusinessException;
import com.granishots.inventory.domain.usecase.SupplyUseCase;
import com.granishots.inventory.infraestructure.mapper.SupplyMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/supplies")
@RequiredArgsConstructor
public class SupplyController {

    private final SupplyUseCase supplyUseCase;
    private final SupplyMapper supplyMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<SupplyResponseDTO>> save(@Valid @RequestBody SupplyRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Insumo registrado exitosamente",
                supplyMapper.toSupplyResponseDTO(supplyUseCase.save(supplyMapper.toSupplyFromDTO(dto)))));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplyResponseDTO>>> findAll() {
        List<SupplyResponseDTO> list = supplyUseCase.findAll().stream().map(supplyMapper::toSupplyResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Insumos obtenidos — total: " + list.size(), list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplyResponseDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Insumo encontrado",
                supplyMapper.toSupplyResponseDTO(supplyUseCase.findById(id))));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<SupplyResponseDTO>>> findLowStock() {
        List<SupplyResponseDTO> list = supplyUseCase.findLowStock().stream().map(supplyMapper::toSupplyResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Insumos con bajo stock — total: " + list.size(), list));
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<ApiResponse<List<SupplyResponseDTO>>> findOutOfStock() {
        List<SupplyResponseDTO> list = supplyUseCase.findOutOfStock().stream().map(supplyMapper::toSupplyResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Insumos sin stock — total: " + list.size(), list));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SupplyResponseDTO>>> findByName(@RequestParam String name) {
        List<SupplyResponseDTO> list = supplyUseCase.findByName(name).stream().map(supplyMapper::toSupplyResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Resultados para '" + name + "' — total: " + list.size(), list));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplyResponseDTO>> update(@PathVariable Long id, @Valid @RequestBody SupplyRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok("Insumo actualizado exitosamente",
                supplyMapper.toSupplyResponseDTO(supplyUseCase.update(id, supplyMapper.toSupplyFromDTO(dto)))));
    }

    @PatchMapping("/{id}/min-stock")
    public ResponseEntity<ApiResponse<SupplyResponseDTO>> updateMinStock(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!body.containsKey("minStock"))
            throw new BusinessException("El campo 'minStock' es obligatorio en el body", 400);
        Double minStock;
        try { minStock = Double.parseDouble(body.get("minStock").toString()); }
        catch (NumberFormatException e) { throw new BusinessException("'minStock' debe ser un número válido", 400); }
        return ResponseEntity.ok(ApiResponse.ok("Stock mínimo actualizado",
                supplyMapper.toSupplyResponseDTO(supplyUseCase.updateMinStock(id, minStock))));
    }

    @PostMapping("/{id}/entry")
    public ResponseEntity<ApiResponse<SupplyResponseDTO>> registerEntry(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!body.containsKey("quantity"))
            throw new BusinessException("El campo 'quantity' es obligatorio en el body", 400);
        Double quantity;
        try { quantity = Double.parseDouble(body.get("quantity").toString()); }
        catch (NumberFormatException e) { throw new BusinessException("'quantity' debe ser un número válido", 400); }
        return ResponseEntity.ok(ApiResponse.ok("Entrada de stock registrada",
                supplyMapper.toSupplyResponseDTO(supplyUseCase.registerEntry(id, quantity))));
    }

    @PostMapping("/{id}/exit")
    public ResponseEntity<ApiResponse<SupplyResponseDTO>> registerExit(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!body.containsKey("quantity"))
            throw new BusinessException("El campo 'quantity' es obligatorio en el body", 400);
        Double quantity;
        try { quantity = Double.parseDouble(body.get("quantity").toString()); }
        catch (NumberFormatException e) { throw new BusinessException("'quantity' debe ser un número válido", 400); }
        return ResponseEntity.ok(ApiResponse.ok("Salida de stock registrada",
                supplyMapper.toSupplyResponseDTO(supplyUseCase.registerExit(id, quantity))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        supplyUseCase.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Insumo con id " + id + " eliminado correctamente", null));
    }
}
