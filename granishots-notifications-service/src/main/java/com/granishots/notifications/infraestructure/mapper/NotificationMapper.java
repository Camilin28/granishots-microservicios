package com.granishots.notifications.infraestructure.mapper;

import com.granishots.notifications.application.dto.NotificationRequestDTO;
import com.granishots.notifications.application.dto.NotificationResponseDTO;
import com.granishots.notifications.application.dto.TemplateRequestDTO;
import com.granishots.notifications.application.dto.TemplateResponseDTO;
import com.granishots.notifications.domain.model.Notification;
import com.granishots.notifications.domain.model.NotificationTemplate;
import com.granishots.notifications.infraestructure.driver_adapters.jpa_repository.TemplateData;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    // ── Notification ──────────────────────────────────────────────────────────

    public Notification toNotificationFromDTO(NotificationRequestDTO dto) {
        return new Notification(
                null,
                dto.getCustomerId(),
                dto.getCustomerName(),
                dto.getCustomerContact(),
                dto.getChannel(),
                dto.getType(),
                dto.getSubject(),
                dto.getMessage(),
                false,
                "PENDING",
                null,
                null
        );
    }

    public NotificationResponseDTO toNotificationResponseDTO(Notification n) {
        return new NotificationResponseDTO(
                n.getCustomerId(),
                n.getCustomerName(),
                n.getCustomerContact(),
                n.getChannel(),
                n.getType(),
                n.getSubject(),
                n.getMessage(),
                n.getStatus(),
                n.getSentAt()
        );
    }

    // ── Template ──────────────────────────────────────────────────────────────

    public TemplateData toTemplateData(NotificationTemplate t) {
        return new TemplateData(
                t.getId(), t.getName(), t.getType(),
                t.getChannel(), t.getSubject(), t.getBody(), t.getActive()
        );
    }

    public NotificationTemplate toTemplate(TemplateData d) {
        return new NotificationTemplate(
                d.getId(), d.getName(), d.getType(),
                d.getChannel(), d.getSubject(), d.getBody(), d.getActive()
        );
    }

    public NotificationTemplate toTemplateFromDTO(TemplateRequestDTO dto) {
        return new NotificationTemplate(
                null, dto.getName(), dto.getType(),
                dto.getChannel(), dto.getSubject(), dto.getBody(), true
        );
    }

    public TemplateResponseDTO toTemplateResponseDTO(NotificationTemplate t) {
        return new TemplateResponseDTO(
                t.getId(), t.getName(), t.getType(),
                t.getChannel(), t.getSubject(), t.getBody(), t.getActive()
        );
    }
}
