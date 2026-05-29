package com.granishots.catalog.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    @DecimalMax(value = "999999", message = "El precio no puede superar 999999")
    private Double price;

    @NotBlank(message = "La categoría es obligatoria")
    @Pattern(regexp = "^(PEQUEÑA|MEDIANO|MEGA)$",
            message = "La categoría debe ser: PEQUEÑA, MEDIANO o MEGA")
    private String category;

    @NotBlank(message = "El tamaño es obligatorio")
    @Pattern(regexp = "^(8|16|24)$",
            message = "El tamaño debe ser: 8, 16 o 24 (onzas)")
    private String size;
}
