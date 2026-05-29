package com.granishots.notifications.infraestructure.mapper;

import com.granishots.notifications.application.dto.NotificationRequestDTO;
import com.granishots.notifications.application.dto.TemplateRequestDTO;
import com.granishots.notifications.domain.model.Notification;
import com.granishots.notifications.domain.model.NotificationTemplate;
import com.granishots.notifications.infraestructure.driver_adapters.jpa_repository.TemplateData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationMapper — Pruebas Unitarias")
class NotificationMapperTest {

    private NotificationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NotificationMapper();
    }

    // ── DTO → Notification ────────────────────────────────────────────────────

    @Test
    @DisplayName("toNotificationFromDTO → todos los campos mapeados correctamente")
    void toNotificationFromDTO() {
        NotificationRequestDTO dto = new NotificationRequestDTO();
        dto.setCustomerId(10L);
        dto.setCustomerName("Ana Torres");
        dto.setCustomerContact("ana@email.com");
        dto.setChannel("EMAIL");
        dto.setType("ORDER_CONFIRMED");
        dto.setSubject("Pedido listo");
        dto.setMessage("Tu pedido está listo.");

        Notification n = mapper.toNotificationFromDTO(dto);

        assertThat(n.getCustomerId()).isEqualTo(10L);
        assertThat(n.getCustomerName()).isEqualTo("Ana Torres");
        assertThat(n.getCustomerContact()).isEqualTo("ana@email.com");
        assertThat(n.getChannel()).isEqualTo("EMAIL");
        assertThat(n.getType()).isEqualTo("ORDER_CONFIRMED");
        assertThat(n.getSubject()).isEqualTo("Pedido listo");
        assertThat(n.getMessage()).isEqualTo("Tu pedido está listo.");
        assertThat(n.getId()).isNull();
        assertThat(n.getStatus()).isEqualTo("PENDING");
        assertThat(n.getRead()).isFalse();
    }

    // ── Notification → ResponseDTO ────────────────────────────────────────────

    @Test
    @DisplayName("toNotificationResponseDTO → campos mapeados y sentAt incluido")
    void toNotificationResponseDTO() {
        LocalDateTime now = LocalDateTime.now();
        Notification n = new Notification(1L, 5L, "Pedro", "pedro@email.com",
                "EMAIL", "PROMO", "Descuento", "30% off", false, "SENT", now, now);

        var dto = mapper.toNotificationResponseDTO(n);

        assertThat(dto.getCustomerId()).isEqualTo(5L);
        assertThat(dto.getCustomerName()).isEqualTo("Pedro");
        assertThat(dto.getStatus()).isEqualTo("SENT");
        assertThat(dto.getSentAt()).isEqualTo(now);
    }

    // ── NotificationTemplate → TemplateData ──────────────────────────────────

    @Test
    @DisplayName("toTemplateData → todos los campos mapeados")
    void toTemplateData() {
        NotificationTemplate t = new NotificationTemplate(
                1L, "Bienvenida", "WELCOME", "EMAIL", "Asunto", "Hola {{name}}", true);

        TemplateData d = mapper.toTemplateData(t);

        assertThat(d.getId()).isEqualTo(1L);
        assertThat(d.getName()).isEqualTo("Bienvenida");
        assertThat(d.getType()).isEqualTo("WELCOME");
        assertThat(d.getChannel()).isEqualTo("EMAIL");
        assertThat(d.getSubject()).isEqualTo("Asunto");
        assertThat(d.getBody()).isEqualTo("Hola {{name}}");
        assertThat(d.getActive()).isTrue();
    }

    // ── TemplateData → NotificationTemplate ──────────────────────────────────

    @Test
    @DisplayName("toTemplate → convierte TemplateData a dominio")
    void toTemplate() {
        TemplateData d = new TemplateData(
                2L, "Promo", "PROMO", "SMS", null, "50% descuento", true);

        NotificationTemplate t = mapper.toTemplate(d);

        assertThat(t.getId()).isEqualTo(2L);
        assertThat(t.getName()).isEqualTo("Promo");
        assertThat(t.getSubject()).isNull();
        assertThat(t.getBody()).isEqualTo("50% descuento");
    }

    // ── TemplateRequestDTO → NotificationTemplate ─────────────────────────────

    @Test
    @DisplayName("toTemplateFromDTO → active=true y id=null")
    void toTemplateFromDTO() {
        TemplateRequestDTO dto = new TemplateRequestDTO();
        dto.setName("Orden lista");
        dto.setType("ORDER_READY");
        dto.setChannel("PUSH");
        dto.setSubject("Tu orden está lista");
        dto.setBody("Pasa a recoger tu pedido.");

        NotificationTemplate t = mapper.toTemplateFromDTO(dto);

        assertThat(t.getId()).isNull();
        assertThat(t.getActive()).isTrue();
        assertThat(t.getName()).isEqualTo("Orden lista");
        assertThat(t.getType()).isEqualTo("ORDER_READY");
        assertThat(t.getChannel()).isEqualTo("PUSH");
    }

    // ── NotificationTemplate → TemplateResponseDTO ───────────────────────────

    @Test
    @DisplayName("toTemplateResponseDTO → todos los campos presentes")
    void toTemplateResponseDTO() {
        NotificationTemplate t = new NotificationTemplate(
                3L, "Cancelado", "CANCELLED", "WHATSAPP", null, "Pedido cancelado.", false);

        var dto = mapper.toTemplateResponseDTO(t);

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getName()).isEqualTo("Cancelado");
        assertThat(dto.getActive()).isFalse();
        assertThat(dto.getSubject()).isNull();
    }
}
