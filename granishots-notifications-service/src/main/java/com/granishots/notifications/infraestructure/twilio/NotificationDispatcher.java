package com.granishots.notifications.infraestructure.twilio;

import com.granishots.notifications.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private final TwilioSmsService smsService;
    private final EmailLogService emailService;

    /**
     * Despacha la notificación al canal correcto.
     * SMS → Twilio real
     * EMAIL → SendGrid (o log simulado si no está configurado)
     * WHATSAPP → Log simulado (requiere Twilio WhatsApp sandbox)
     * PUSH → Log simulado (requiere FCM/APNs)
     *
     * @param channel         Canal: SMS, EMAIL, WHATSAPP, PUSH
     * @param customerContact Destino: teléfono o email según el canal
     * @param subject         Asunto (para EMAIL)
     * @param message         Cuerpo del mensaje
     * @return                Referencia del envío (SID de Twilio o "SIMULATED")
     */
    public String dispatch(String channel, String customerContact, String subject, String message) {
        if (customerContact == null || customerContact.trim().isEmpty()) {
            throw new BusinessException(
                "El campo 'customerContact' es obligatorio para enviar por " + channel, 400);
        }

        return switch (channel.toUpperCase()) {
            case "SMS" -> dispatchSms(customerContact, message);
            case "EMAIL" -> dispatchEmail(customerContact, subject, message);
            case "WHATSAPP" -> dispatchWhatsApp(customerContact, message);
            case "PUSH" -> dispatchPush(customerContact, message);
            default -> throw new BusinessException(
                "Canal no soportado: " + channel + ". Canales válidos: SMS, EMAIL, WHATSAPP, PUSH", 400);
        };
    }

    private String dispatchSms(String phone, String message) {
        log.info("📱 Despachando SMS → {}", phone);
        String sid = smsService.sendSms(phone, message);
        log.info("✅ SMS despachado | SID: {}", sid);
        return sid;
    }

    private String dispatchEmail(String email, String subject, String message) {
        log.info("📧 Despachando EMAIL → {}", email);
        emailService.sendEmail(email, subject != null ? subject : "Notificación GraniShots", message);
        return "EMAIL_DISPATCHED";
    }

    private String dispatchWhatsApp(String phone, String message) {
        log.info("💬 [WHATSAPP SIMULADO] → {} | Mensaje: {}", phone,
                message.length() > 50 ? message.substring(0, 50) + "..." : message);
        log.info("ℹ️  Para WhatsApp real: activar Twilio WhatsApp Sandbox en console.twilio.com");
        return "WHATSAPP_SIMULATED";
    }

    private String dispatchPush(String token, String message) {
        log.info("🔔 [PUSH SIMULADO] → token: {} | Mensaje: {}", token,
                message.length() > 50 ? message.substring(0, 50) + "..." : message);
        log.info("ℹ️  Para Push real: integrar Firebase FCM o Apple APNs");
        return "PUSH_SIMULATED";
    }
}
