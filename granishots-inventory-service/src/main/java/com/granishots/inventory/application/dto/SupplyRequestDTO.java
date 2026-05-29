package com.granishots.inventory.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SupplyRequestDTO {

    @NotBlank(message = "El nombre del insumo es obligatorio")
    private String name;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Pattern(regexp = "^(KG|G|L|ML|UNIDAD|BOLSA|CAJA)$",
            message = "Unidad debe ser: KG, G, L, ML, UNIDAD, BOLSA o CAJA")
    private String unit;

    @NotNull(message = "El stock actual es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Double currentStock;

    @NotNull(message = "El stock mínimo es obligatorio")
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private Double minStock;

    private String supplier;

    @PositiveOrZero(message = "El precio de compra no puede ser negativo")
    private Double purchasePrice;
}
