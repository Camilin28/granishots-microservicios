package com.granishots.notifications.infraestructure.entry_points;

import com.granishots.notifications.application.dto.ApiResponse;
import com.granishots.notifications.application.dto.NotificationRequestDTO;
import com.granishots.notifications.application.dto.NotificationResponseDTO;
import com.granishots.notifications.domain.model.Notification;
import com.granishots.notifications.domain.usecase.NotificationUseCase;
import com.granishots.notifications.infraestructure.mapper.NotificationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationUseCase notificationUseCase;
    private final NotificationMapper notificationMapper;

    // ── Envío individual ──────────────────────────────────────────────────────

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> send(
            @Valid @RequestBody NotificationRequestDTO dto) {
        Notification sent = notificationUseCase.send(notificationMapper.toNotificationFromDTO(dto));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "Notificación enviada exitosamente por " + sent.getChannel(),
                        notificationMapper.toNotificationResponseDTO(sent)));
    }

    // ── Envío masivo ──────────────────────────────────────────────────────────

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<NotificationResponseDTO>>> sendBulk(
            @Valid @RequestBody List<NotificationRequestDTO> dtos) {
        List<Notification> notifications = dtos.stream()
                .map(notificationMapper::toNotificationFromDTO).toList();
        List<NotificationResponseDTO> result = notificationUseCase.sendBulk(notifications).stream()
                .map(notificationMapper::toNotificationResponseDTO).toList();
        long sent   = result.stream().filter(n -> "SENT".equals(n.getStatus())).count();
        long failed = result.stream().filter(n -> "FAILED".equals(n.getStatus())).count();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "Envío masivo completado — Enviados: " + sent + " | Fallidos: " + failed,
                        result));
    }
}
