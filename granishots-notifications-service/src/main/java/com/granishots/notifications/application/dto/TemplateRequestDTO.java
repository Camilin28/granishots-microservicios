package com.granishots.notifications.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TemplateRequestDTO {

    @NotBlank(message = "El nombre de la plantilla es obligatorio")
    private String name;

    @NotBlank(message = "El tipo es obligatorio")
    @Pattern(regexp = "^(ORDER_CONFIRMED|ORDER_READY|PROMO|REMINDER|WELCOME|CANCELLED)$",
            message = "Tipo inválido. Use: ORDER_CONFIRMED, ORDER_READY, PROMO, REMINDER, WELCOME, CANCELLED")
    private String type;

    @NotBlank(message = "El canal es obligatorio")
    @Pattern(regexp = "^(EMAIL|SMS|PUSH|WHATSAPP)$",
            message = "Canal inválido. Use: EMAIL, SMS, PUSH, WHATSAPP")
    private String channel;

    private String subject;

    @NotBlank(message = "El cuerpo de la plantilla es obligatorio")
    private String body;
}
