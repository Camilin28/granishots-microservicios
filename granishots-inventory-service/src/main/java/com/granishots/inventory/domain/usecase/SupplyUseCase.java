package com.granishots.inventory.domain.usecase;

import com.granishots.inventory.domain.exception.BusinessException;
import com.granishots.inventory.domain.model.Supply;
import com.granishots.inventory.domain.model.gateway.SupplyGateway;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class SupplyUseCase {

    private final SupplyGateway supplyGateway;

    private static final List<String> UNIDADES_VALIDAS = List.of("KG","G","L","ML","UNIDAD","BOLSA","CAJA");
    private static final double STOCK_MAXIMO = 999_999;
    private static final int NOMBRE_MAX = 100;

    public Supply save(Supply supply) {
        validarSupply(supply);
        List<Supply> existentes = supplyGateway.findByName(supply.getName().trim());
        boolean duplicado = existentes.stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(supply.getName().trim()));
        if (duplicado)
            throw new BusinessException("Ya existe un insumo con el nombre '" + supply.getName().trim() + "'", 409);
        supply.setName(supply.getName().trim());
        supply.setUnit(supply.getUnit().trim().toUpperCase());
        return supplyGateway.save(supply);
    }

    public Supply findById(Long id) {
        validarId(id);
        Supply supply = supplyGateway.findById(id);
        if (supply == null)
            throw new BusinessException("No existe un insumo con el id: " + id, 404);
        return supply;
    }

    public List<Supply> findAll() { return supplyGateway.findAll(); }

    public List<Supply> findLowStock() { return supplyGateway.findLowStock(); }

    public List<Supply> findOutOfStock() { return supplyGateway.findOutOfStock(); }

    public List<Supply> findByName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new BusinessException("El término de búsqueda no puede estar vacío", 400);
        if (name.trim().length() < 2)
            throw new BusinessException("El término debe tener al menos 2 caracteres", 400);
        return supplyGateway.findByName(name.trim());
    }

    public Supply update(Long id, Supply supply) {
        validarId(id);
        validarSupply(supply);
        Supply existente = supplyGateway.findById(id);
        if (existente == null)
            throw new BusinessException("No existe un insumo con el id: " + id, 404);
        List<Supply> mismoNombre = supplyGateway.findByName(supply.getName().trim());
        boolean duplicado = mismoNombre.stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(supply.getName().trim()) && !s.getId().equals(id));
        if (duplicado)
            throw new BusinessException("Ya existe otro insumo con el nombre '" + supply.getName().trim() + "'", 409);
        supply.setId(id);
        supply.setName(supply.getName().trim());
        supply.setUnit(supply.getUnit().trim().toUpperCase());
        return supplyGateway.update(supply);
    }

    public void deleteById(Long id) {
        validarId(id);
        if (supplyGateway.findById(id) == null)
            throw new BusinessException("No existe un insumo con el id: " + id, 404);
        supplyGateway.deleteById(id);
    }

    public Supply updateMinStock(Long id, Double minStock) {
        validarId(id);
        if (minStock == null)
            throw new BusinessException("El stock mínimo no puede ser nulo", 400);
        if (minStock < 0)
            throw new BusinessException("El stock mínimo no puede ser negativo. Recibido: " + minStock, 400);
        if (minStock > STOCK_MAXIMO)
            throw new BusinessException("El stock mínimo no puede superar " + STOCK_MAXIMO, 400);
        Supply supply = supplyGateway.findById(id);
        if (supply == null)
            throw new BusinessException("No existe un insumo con el id: " + id, 404);
        if (minStock > supply.getCurrentStock())
            throw new BusinessException(
                "El stock mínimo (" + minStock + ") no puede ser mayor al stock actual (" + supply.getCurrentStock() + ")", 400);
        return supplyGateway.updateMinStock(id, minStock);
    }

    public Supply registerEntry(Long id, Double quantity) {
        validarId(id);
        validarCantidad(quantity, "entrada");
        Supply supply = supplyGateway.findById(id);
        if (supply == null)
            throw new BusinessException("No existe un insumo con el id: " + id, 404);
        double nuevoStock = supply.getCurrentStock() + quantity;
        if (nuevoStock > STOCK_MAXIMO)
            throw new BusinessException(
                "La entrada generaría un stock de " + nuevoStock + " que supera el máximo permitido de " + STOCK_MAXIMO, 400);
        return supplyGateway.addStock(id, quantity);
    }

    public Supply registerExit(Long id, Double quantity) {
        validarId(id);
        validarCantidad(quantity, "salida");
        Supply supply = supplyGateway.findById(id);
        if (supply == null)
            throw new BusinessException("No existe un insumo con el id: " + id, 404);
        if (supply.getCurrentStock() <= 0)
            throw new BusinessException("El insumo '" + supply.getName() + "' no tiene stock disponible", 400);
        if (supply.getCurrentStock() < quantity)
            throw new BusinessException(
                "Stock insuficiente. Disponible: " + supply.getCurrentStock() + " " + supply.getUnit()
                + " — Solicitado: " + quantity + " " + supply.getUnit(), 400);
        return supplyGateway.subtractStock(id, quantity);
    }

    private void validarId(Long id) {
        if (id == null) throw new BusinessException("El ID no puede ser nulo", 400);
        if (id <= 0) throw new BusinessException("El ID debe ser un número positivo. Recibido: " + id, 400);
    }

    private void validarCantidad(Double quantity, String tipo) {
        if (quantity == null) throw new BusinessException("La cantidad de " + tipo + " no puede ser nula", 400);
        if (quantity <= 0) throw new BusinessException("La cantidad de " + tipo + " debe ser mayor a cero. Recibido: " + quantity, 400);
        if (quantity > STOCK_MAXIMO) throw new BusinessException("La cantidad de " + tipo + " no puede superar " + STOCK_MAXIMO, 400);
    }

    private void validarSupply(Supply supply) {
        if (supply == null) throw new BusinessException("El cuerpo del insumo no puede ser nulo", 400);
        if (supply.getName() == null || supply.getName().trim().isEmpty())
            throw new BusinessException("El nombre del insumo no puede estar vacío", 400);
        if (supply.getName().trim().length() > NOMBRE_MAX)
            throw new BusinessException("El nombre no puede superar " + NOMBRE_MAX + " caracteres", 400);
        if (supply.getUnit() == null || supply.getUnit().trim().isEmpty())
            throw new BusinessException("La unidad de medida no puede estar vacía", 400);
        String unit = supply.getUnit().trim().toUpperCase();
        if (!UNIDADES_VALIDAS.contains(unit))
            throw new BusinessException("Unidad inválida: '" + supply.getUnit() + "'. Válidas: " + UNIDADES_VALIDAS, 400);
        if (supply.getCurrentStock() == null)
            throw new BusinessException("El stock actual es obligatorio", 400);
        if (supply.getCurrentStock() < 0)
            throw new BusinessException("El stock actual no puede ser negativo. Recibido: " + supply.getCurrentStock(), 400);
        if (supply.getCurrentStock() > STOCK_MAXIMO)
            throw new BusinessException("El stock no puede superar " + STOCK_MAXIMO, 400);
        if (supply.getMinStock() == null)
            throw new BusinessException("El stock mínimo es obligatorio", 400);
        if (supply.getMinStock() < 0)
            throw new BusinessException("El stock mínimo no puede ser negativo. Recibido: " + supply.getMinStock(), 400);
        if (supply.getMinStock() > supply.getCurrentStock())
            throw new BusinessException(
                "El stock mínimo (" + supply.getMinStock() + ") no puede ser mayor al stock actual (" + supply.getCurrentStock() + ")", 400);
        if (supply.getPurchasePrice() != null && supply.getPurchasePrice() < 0)
            throw new BusinessException("El precio de compra no puede ser negativo", 400);
    }
}
