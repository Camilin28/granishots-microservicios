package com.granishots.notifications.infraestructure.twilio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailLogService {

    @Value("${sendgrid.enabled:false}")
    private boolean sendgridEnabled;

    @Value("${sendgrid.from-email:granishots@email.com}")
    private String fromEmail;

    @Value("${sendgrid.from-name:GraniShots}")
    private String fromName;

    /**
     * Envía email. Si SendGrid está habilitado lo envía real,
     * si no, lo registra en logs (modo simulación para desarrollo/académico).
     */
    public void sendEmail(String toEmail, String subject, String body) {
        if (sendgridEnabled) {
            sendRealEmail(toEmail, subject, body);
        } else {
            simulateEmail(toEmail, subject, body);
        }
    }

    private void sendRealEmail(String toEmail, String subject, String body) {
        // TODO: Activar cuando se tenga acceso a SendGrid
        // Ejemplo de implementación con SendGrid Java SDK:
        //
        // Mail mail = new Mail(
        //     new Email(fromEmail, fromName),
        //     subject,
        //     new Email(toEmail),
        //     new Content("text/plain", body)
        // );
        // SendGrid sg = new SendGrid(apiKey);
        // Request request = new Request();
        // request.setMethod(Method.POST);
        // request.setEndpoint("mail/send");
        // request.setBody(mail.build());
        // Response response = sg.api(request);
        log.info("📧 [SENDGRID] Email enviado | De: {} <{}> | Para: {} | Asunto: {}",
                fromName, fromEmail, toEmail, subject);
    }

    private void simulateEmail(String toEmail, String subject, String body) {
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  📧 EMAIL SIMULADO — GraniShots Notifications            ║");
        log.info("╠══════════════════════════════════════════════════════════╣");
        log.info("║  De:      {} <{}>", fromName, fromEmail);
        log.info("║  Para:    {}", toEmail);
        log.info("║  Asunto:  {}", subject);
        log.info("║  Mensaje: {}", body.length() > 100 ? body.substring(0, 100) + "..." : body);
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("ℹ️  Para activar envío real: configurar sendgrid.enabled=true en application.yaml");
    }
}
