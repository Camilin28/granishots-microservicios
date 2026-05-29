package com.granishots.notifications.infraestructure.twilio;

import com.granishots.notifications.domain.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationDispatcher — Pruebas Unitarias")
class NotificationDispatcherTest {

    @Mock
    private TwilioSmsService smsService;

    @Mock
    private EmailLogService emailService;

    @InjectMocks
    private NotificationDispatcher dispatcher;

    // ─────────────────────────────────────────────────────────────────────────
    // SMS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Canal SMS")
    class Sms {

        @Test
        @DisplayName("SMS exitoso → retorna SID de Twilio")
        void smsExitoso() {
            when(smsService.sendSms(anyString(), anyString())).thenReturn("SM_ABC123");

            String ref = dispatcher.dispatch("SMS", "+573001234567", null, "Hola!");

            assertThat(ref).isEqualTo("SM_ABC123");
            verify(smsService).sendSms("+573001234567", "Hola!");
        }

        @Test
        @DisplayName("SMS en minúsculas → canal normalizado, SMS enviado")
        void smsEnMinusculas() {
            when(smsService.sendSms(anyString(), anyString())).thenReturn("SID");
            assertThatCode(() -> dispatcher.dispatch("sms", "+573001234567", null, "msg"))
                    .doesNotThrowAnyException();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EMAIL
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Canal EMAIL")
    class Email {

        @Test
        @DisplayName("EMAIL exitoso → retorna EMAIL_DISPATCHED")
        void emailExitoso() {
            doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

            String ref = dispatcher.dispatch("EMAIL", "a@b.com", "Asunto", "Mensaje");

            assertThat(ref).isEqualTo("EMAIL_DISPATCHED");
            verify(emailService).sendEmail("a@b.com", "Asunto", "Mensaje");
        }

        @Test
        @DisplayName("EMAIL sin subject → se usa asunto por defecto")
        void emailSinSubject() {
            doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

            dispatcher.dispatch("EMAIL", "a@b.com", null, "Mensaje");

            verify(emailService).sendEmail(eq("a@b.com"), eq("Notificación GraniShots"), eq("Mensaje"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WHATSAPP y PUSH (simulados)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Canales simulados")
    class Simulados {

        @Test
        @DisplayName("WHATSAPP → retorna WHATSAPP_SIMULATED")
        void whatsapp() {
            String ref = dispatcher.dispatch("WHATSAPP", "+573001234567", null, "msg");
            assertThat(ref).isEqualTo("WHATSAPP_SIMULATED");
            verifyNoInteractions(smsService, emailService);
        }

        @Test
        @DisplayName("PUSH → retorna PUSH_SIMULATED")
        void push() {
            String ref = dispatcher.dispatch("PUSH", "device-token", null, "msg");
            assertThat(ref).isEqualTo("PUSH_SIMULATED");
            verifyNoInteractions(smsService, emailService);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Validaciones generales
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Validaciones")
    class Validaciones {

        @Test
        @DisplayName("Canal no soportado → BusinessException 400")
        void canalNoSoportado() {
            assertThatThrownBy(() -> dispatcher.dispatch("FAX", "+57300", null, "msg"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("FAX")
                    .extracting(e -> ((BusinessException) e).getHttpStatus())
                    .isEqualTo(400);
        }

        @Test
        @DisplayName("customerContact nulo → BusinessException 400")
        void contactoNulo() {
            assertThatThrownBy(() -> dispatcher.dispatch("SMS", null, null, "msg"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("customerContact")
                    .extracting(e -> ((BusinessException) e).getHttpStatus())
                    .isEqualTo(400);
        }

        @Test
        @DisplayName("customerContact vacío → BusinessException 400")
        void contactoVacio() {
            assertThatThrownBy(() -> dispatcher.dispatch("EMAIL", "  ", null, "msg"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("customerContact");
        }
    }
}
