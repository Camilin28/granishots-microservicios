package com.granishots.notifications.infraestructure.entry_points;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.granishots.notifications.application.dto.NotificationRequestDTO;
import com.granishots.notifications.domain.exception.BusinessException;
import com.granishots.notifications.domain.model.Notification;
import com.granishots.notifications.domain.usecase.NotificationUseCase;
import com.granishots.notifications.infraestructure.mapper.NotificationMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@DisplayName("NotificationController — Pruebas Web (MockMvc)")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationUseCase notificationUseCase;

    @MockBean
    private NotificationMapper notificationMapper;

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private NotificationRequestDTO buildRequestDTO() {
        NotificationRequestDTO dto = new NotificationRequestDTO();
        dto.setCustomerId(1L);
        dto.setCustomerName("Juan");
        dto.setCustomerContact("juan@email.com");
        dto.setChannel("EMAIL");
        dto.setType("ORDER_CONFIRMED");
        dto.setMessage("Tu pedido ha sido confirmado.");
        return dto;
    }

    private Notification buildSentNotification() {
        Notification n = new Notification();
        n.setCustomerId(1L);
        n.setCustomerName("Juan");
        n.setCustomerContact("juan@email.com");
        n.setChannel("EMAIL");
        n.setType("ORDER_CONFIRMED");
        n.setMessage("Tu pedido ha sido confirmado.");
        n.setStatus("SENT");
        n.setSentAt(LocalDateTime.now());
        return n;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/notifications/send
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /send")
    class Send {

        @Test
        @DisplayName("Envío exitoso → 201 Created con status SENT")
        void envioExitoso() throws Exception {
            Notification domain = buildSentNotification();
            when(notificationMapper.toNotificationFromDTO(any())).thenReturn(domain);
            when(notificationUseCase.send(any())).thenReturn(domain);
            when(notificationMapper.toNotificationResponseDTO(any())).thenReturn(
                    new com.granishots.notifications.application.dto.NotificationResponseDTO(
                            1L, "Juan", "juan@email.com", "EMAIL", "ORDER_CONFIRMED",
                            null, "Tu pedido ha sido confirmado.", "SENT", domain.getSentAt())
            );

            mockMvc.perform(post("/api/v1/notifications/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequestDTO())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.status").value("SENT"));
        }

        @Test
        @DisplayName("Body vacío → 400 Bad Request (validación @Valid)")
        void bodyVacio() throws Exception {
            mockMvc.perform(post("/api/v1/notifications/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Canal inválido en el DTO → 400 Bad Request")
        void canalInvalido() throws Exception {
            NotificationRequestDTO dto = buildRequestDTO();
            dto.setChannel("FAX");

            mockMvc.perform(post("/api/v1/notifications/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Use case lanza BusinessException 502 → 502 retornado")
        void useCaseLanzaBusinessException() throws Exception {
            Notification domain = buildSentNotification();
            when(notificationMapper.toNotificationFromDTO(any())).thenReturn(domain);
            when(notificationUseCase.send(any()))
                    .thenThrow(new BusinessException("Error Twilio", 502));

            mockMvc.perform(post("/api/v1/notifications/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequestDTO())))
                    .andExpect(status().isBadGateway());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/notifications/bulk
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /bulk")
    class Bulk {

        @Test
        @DisplayName("Envío masivo exitoso → 201 con resumen")
        void envioMasivoExitoso() throws Exception {
            Notification domain = buildSentNotification();
            when(notificationMapper.toNotificationFromDTO(any())).thenReturn(domain);
            when(notificationUseCase.sendBulk(any())).thenReturn(List.of(domain, domain));
            when(notificationMapper.toNotificationResponseDTO(any())).thenReturn(
                    new com.granishots.notifications.application.dto.NotificationResponseDTO(
                            1L, "Juan", "juan@email.com", "EMAIL", "ORDER_CONFIRMED",
                            null, "msg", "SENT", domain.getSentAt())
            );

            List<NotificationRequestDTO> dtos = List.of(buildRequestDTO(), buildRequestDTO());

            mockMvc.perform(post("/api/v1/notifications/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtos)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }
}
