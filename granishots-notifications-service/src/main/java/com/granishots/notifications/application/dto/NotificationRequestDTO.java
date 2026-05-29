package com.granishots.notifications.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NotificationRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long customerId;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String customerName;

    private String customerContact;

    @NotBlank(message = "El canal es obligatorio")
    @Pattern(regexp = "^(EMAIL|SMS|PUSH|WHATSAPP)$",
            message = "El canal debe ser EMAIL, SMS, PUSH o WHATSAPP")
    private String channel;

    @NotBlank(message = "El tipo es obligatorio")
    private String type;

    private String subject;

    @NotBlank(message = "El mensaje es obligatorio")
    private String message;
}
