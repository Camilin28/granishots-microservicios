package com.granishots.notifications.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerContact;   // email, phone, etc.
    private String channel;           // EMAIL, SMS, PUSH, WHATSAPP
    private String type;              // ORDER_CONFIRMED, ORDER_READY, PROMO, REMINDER
    private String subject;
    private String message;
    private Boolean read;
    private String status;            // PENDING, SENT, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
