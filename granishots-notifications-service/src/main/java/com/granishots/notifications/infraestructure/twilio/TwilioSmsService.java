package com.granishots.notifications.infraestructure.twilio;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioSmsService {

    @Value("${twilio.phone-number}")
    private String fromNumber;

    /**
     * Envía SMS real usando Twilio.
     * Cuenta trial: solo envía a números verificados en Twilio Console.
     * Número verificado destino: +573232466465
     */
    public String sendSms(String toPhone, String message) {
        try {
            String normalizedPhone = normalizePhone(toPhone);

            log.info("📱 Enviando SMS | De: {} → Para: {}", fromNumber, normalizedPhone);

            Message twilioMessage = Message.creator(
                    new PhoneNumber(normalizedPhone),
                    new PhoneNumber(fromNumber),
                    message
            ).create();

            log.info("✅ SMS enviado | SID: {} | Estado: {} | Destino: {}",
                    twilioMessage.getSid(),
                    twilioMessage.getStatus(),
                    normalizedPhone);

            return twilioMessage.getSid();

        } catch (com.twilio.exception.ApiException e) {
            log.error("❌ Twilio API Error | Código: {} | Mensaje: {}", e.getCode(), e.getMessage());
            // Error 21608: número destino no verificado en cuenta trial
            if (e.getCode() == 21608) {
                throw new RuntimeException(
                    "Número no verificado en cuenta Twilio Trial. " +
                    "Ve a console.twilio.com → Phone Numbers → Verified Caller IDs → agrega " + toPhone);
            }
            throw new RuntimeException("Error Twilio al enviar SMS: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error inesperado al enviar SMS: {}", e.getMessage());
            throw new RuntimeException("Error al enviar SMS: " + e.getMessage());
        }
    }

    /**
     * Normaliza el número al formato E.164 internacional.
     * Soporta: 3232466465, +573232466465, 573232466465
     */
    private String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty())
            throw new RuntimeException("El número de teléfono no puede estar vacío para SMS");

        String cleaned = phone.trim().replaceAll("[^0-9+]", "");

        if (cleaned.startsWith("+"))    return cleaned;
        if (cleaned.startsWith("57") && cleaned.length() == 12) return "+" + cleaned;
        if (cleaned.length() == 10 && cleaned.startsWith("3")) return "+57" + cleaned;
        return "+" + cleaned;
    }
}
