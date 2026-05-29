package com.granishots.notifications.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationTemplate {
    private Long id;
    private String name;
    private String type;        // ORDER_CONFIRMED, ORDER_READY, PROMO, REMINDER, WELCOME
    private String channel;     // EMAIL, SMS, WHATSAPP, PUSH
    private String subject;
    private String body;        // Supports {{customerName}}, {{orderNumber}}, etc.
    private Boolean active;
}
