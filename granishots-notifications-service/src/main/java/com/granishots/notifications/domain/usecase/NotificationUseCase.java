package com.granishots.notifications.domain.usecase;

import com.granishots.notifications.domain.exception.BusinessException;
import com.granishots.notifications.domain.model.Notification;
import com.granishots.notifications.infraestructure.twilio.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class NotificationUseCase {

    private final NotificationDispatcher dispatcher;

    private static final List<String> VALID_CHANNELS = List.of("EMAIL", "SMS", "PUSH", "WHATSAPP");
    private static final int MSG_MAX   = 1000;
    private static final int BULK_MAX  = 200;

    // ── Envío individual ──────────────────────────────────────────────────────

    /**
     * Envía una notificación por el canal especificado.
     * NO guarda en base de datos — envío directo a Twilio/Email.
     */
    public Notification send(Notification notification) {
        validarNotificacion(notification);

        notification.setCreatedAt(LocalDateTime.now());
        notification.setSentAt(LocalDateTime.now());
        notification.setRead(false);

        try {
            String ref = dispatcher.dispatch(
                    notification.getChannel(),
                    notification.getCustomerContact(),
                    notification.getSubject(),
                    notification.getMessage()
            );
            notification.setStatus("SENT");
            log.info("✅ Notificación enviada | Cliente: {} | Canal: {} | Ref: {}",
                    notification.getCustomerName(), notification.getChannel(), ref);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            notification.setStatus("FAILED");
            log.error("❌ Fallo al enviar notificación | Canal: {} | Error: {}",
                    notification.getChannel(), e.getMessage());
            throw new BusinessException("Error al enviar notificación por "
                    + notification.getChannel() + ": " + e.getMessage(), 502);
        }
        return notification;
    }

    // ── Envío masivo ──────────────────────────────────────────────────────────

    /**
     * Envía múltiples notificaciones. Continúa aunque alguna falle,
     * registrando errores individuales en el resultado.
     */
    public List<Notification> sendBulk(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty())
            throw new BusinessException("La lista de notificaciones no puede estar vacía", 400);
        if (notifications.size() > BULK_MAX)
            throw new BusinessException(
                "No se pueden enviar más de " + BULK_MAX + " notificaciones a la vez. Recibido: "
                + notifications.size(), 400);

        List<Notification> results = new ArrayList<>();
        int sent = 0, failed = 0;

        for (Notification n : notifications) {
            try {
                results.add(send(n));
                sent++;
            } catch (Exception e) {
                n.setStatus("FAILED");
                n.setCreatedAt(LocalDateTime.now());
                results.add(n);
                failed++;
                log.warn("⚠️ Fallo en envío masivo para {}: {}", n.getCustomerName(), e.getMessage());
            }
        }
        log.info("📊 Envío masivo completado | Enviados: {} | Fallidos: {}", sent, failed);
        return results;
    }

    // ── Validaciones ──────────────────────────────────────────────────────────

    private void validarNotificacion(Notification n) {
        if (n == null)
            throw new BusinessException("El cuerpo de la notificación no puede ser nulo", 400);
        if (n.getCustomerId() == null)
            throw new BusinessException("El ID del cliente es obligatorio", 400);
        if (n.getCustomerId() <= 0)
            throw new BusinessException("El ID del cliente debe ser positivo", 400);
        if (n.getCustomerName() == null || n.getCustomerName().trim().isEmpty())
            throw new BusinessException("El nombre del cliente es obligatorio", 400);
        if (n.getChannel() == null || n.getChannel().trim().isEmpty())
            throw new BusinessException("El canal de notificación es obligatorio", 400);
        String channel = n.getChannel().trim().toUpperCase();
        if (!VALID_CHANNELS.contains(channel))
            throw new BusinessException(
                "Canal inválido: '" + n.getChannel() + "'. Válidos: " + VALID_CHANNELS, 400);
        n.setChannel(channel);
        if (n.getMessage() == null || n.getMessage().trim().isEmpty())
            throw new BusinessException("El mensaje no puede estar vacío", 400);
        if (n.getMessage().trim().length() > MSG_MAX)
            throw new BusinessException(
                "El mensaje no puede superar " + MSG_MAX + " caracteres. Actual: "
                + n.getMessage().length(), 400);
        if (n.getType() == null || n.getType().trim().isEmpty())
            throw new BusinessException("El tipo de notificación es obligatorio", 400);
        if ("EMAIL".equals(channel)) {
            if (n.getCustomerContact() == null || n.getCustomerContact().trim().isEmpty())
                throw new BusinessException(
                    "Para notificaciones por EMAIL el campo 'customerContact' (email) es obligatorio", 400);
            if (!n.getCustomerContact().contains("@"))
                throw new BusinessException(
                    "El email de contacto no tiene un formato válido: '" + n.getCustomerContact() + "'", 400);
        }
        if ("SMS".equals(channel)) {
            if (n.getCustomerContact() == null || n.getCustomerContact().trim().isEmpty())
                throw new BusinessException(
                    "Para notificaciones por SMS el campo 'customerContact' (teléfono) es obligatorio", 400);
        }
    }
}
