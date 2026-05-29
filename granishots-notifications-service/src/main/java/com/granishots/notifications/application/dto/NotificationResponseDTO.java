package com.granishots.notifications.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDTO {
    private Long customerId;
    private String customerName;
    private String customerContact;
    private String channel;
    private String type;
    private String subject;
    private String message;
    private String status;        // SENT o FAILED
    private LocalDateTime sentAt;
    // NO se incluye ID porque no se persiste en BD
}
