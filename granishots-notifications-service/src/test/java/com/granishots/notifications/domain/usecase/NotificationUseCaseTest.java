package com.granishots.notifications.domain.usecase;

import com.granishots.notifications.domain.exception.BusinessException;
import com.granishots.notifications.domain.model.Notification;
import com.granishots.notifications.infraestructure.twilio.NotificationDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationUseCase — Pruebas Unitarias")
class NotificationUseCaseTest {

    @Mock
    private NotificationDispatcher dispatcher;

    private NotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new NotificationUseCase(dispatcher);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Notification buildNotification(String channel, String contact) {
        Notification n = new Notification();
        n.setCustomerId(1L);
        n.setCustomerName("Juan Pérez");
        n.setCustomerContact(contact);
        n.setChannel(channel);
        n.setType("ORDER_CONFIRMED");
        n.setSubject("Pedido confirmado");
        n.setMessage("Tu pedido ha sido confirmado.");
        return n;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENVÍO INDIVIDUAL — casos felices
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("send() — casos exitosos")
    class SendExitoso {

        @Test
        @DisplayName("Envío por EMAIL exitoso → status SENT")
        void envioEmailExitoso() {
            when(dispatcher.dispatch(eq("EMAIL"), anyString(), anyString(), anyString()))
                    .thenReturn("EMAIL_DISPATCHED");

            Notification result = useCase.send(buildNotification("EMAIL", "juan@email.com"));

            assertThat(result.getStatus()).isEqualTo("SENT");
            assertThat(result.getSentAt()).isNotNull();
            assertThat(result.getCreatedAt()).isNotNull();
            verify(dispatcher).dispatch("EMAIL", "juan@email.com", "Pedido confirmado", "Tu pedido ha sido confirmado.");
        }

        @Test
        @DisplayName("Envío por SMS exitoso → status SENT")
        void envioSmsExitoso() {
            when(dispatcher.dispatch(eq("SMS"), anyString(), anyString(), anyString()))
                    .thenReturn("SM123456");

            Notification result = useCase.send(buildNotification("SMS", "+573001234567"));

            assertThat(result.getStatus()).isEqualTo("SENT");
            verify(dispatcher).dispatch(eq("SMS"), eq("+573001234567"), any(), anyString());
        }

        @Test
        @DisplayName("Envío por PUSH exitoso → status SENT")
        void envioPushExitoso() {
            when(dispatcher.dispatch(eq("PUSH"), anyString(), anyString(), anyString()))
                    .thenReturn("PUSH_SIMULATED");

            Notification result = useCase.send(buildNotification("PUSH", "device-token-xyz"));

            assertThat(result.getStatus()).isEqualTo("SENT");
        }

        @Test
        @DisplayName("Envío por WHATSAPP exitoso → status SENT")
        void envioWhatsappExitoso() {
            when(dispatcher.dispatch(eq("WHATSAPP"), anyString(), anyString(), anyString()))
                    .thenReturn("WHATSAPP_SIMULATED");

            Notification result = useCase.send(buildNotification("WHATSAPP", "+573001234567"));

            assertThat(result.getStatus()).isEqualTo("SENT");
        }

        @Test
        @DisplayName("Canal en minúsculas debe normalizarse a mayúsculas")
        void canalEnMinusculasNormalizado() {
            when(dispatcher.dispatch(eq("EMAIL"), anyString(), anyString(), anyString()))
                    .thenReturn("EMAIL_DISPATCHED");

            Notification n = buildNotification("email", "test@test.com");
            Notification result = useCase.send(n);

            assertThat(result.getChannel()).isEqualTo("EMAIL");
            assertThat(result.getStatus()).isEqualTo("SENT");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENVÍO INDIVIDUAL — validaciones (casos de error)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("send() — validaciones de entrada")
    class SendValidaciones {

        @Test
        @DisplayName("Notificación nula → BusinessException 400")
        void notificacionNula() {
            assertThatThrownBy(() -> useCase.send(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nulo")
                    .extracting(e -> ((BusinessException) e).getHttpStatus())
                    .isEqualTo(400);
        }

        @Test
        @DisplayName("customerId nulo → BusinessException 400")
        void customerIdNulo() {
            Notification n = buildNotification("EMAIL", "a@b.com");
            n.setCustomerId(null);
            assertThatThrownBy(() -> useCase.send(n))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ID del cliente");
        }

        @Test
        @DisplayName("customerId cero → BusinessException 400")
        void customerIdCero() {
            Notification n = buildNotification("EMAIL", "a@b.com");
            n.setCustomerId(0L);
            assertThatThrownBy(() -> useCase.send(n))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("positivo");
        }

        @Test
        @DisplayName("customerName vacío → BusinessException 400")
        void customerNameVacio() {
            Notification n = buildNotification("EMAIL", "a@b.com");
            n.setCustomerName("  ");
            assertThatThrownBy(() -> useCase.send(n))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nombre del cliente");
        }

        @Test
        @DisplayName("Canal inválido → BusinessException 400")
        void canalInvalido() {
            Notification n = buildNotification("FAX", "a@b.com");
            assertThatThrownBy(() -> useCase.send(n))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("FAX");
        }

        @Test
        @DisplayName("Mensaje vacío → BusinessException 400")
        void mensajeVacio() {
            Notification n = buildNotification("EMAIL", "a@b.com");
            n.setMessage("  ");
            assertThatThrownBy(() -> useCase.send(n))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("mensaje");
        }

        @Test
        @DisplayName("Mensaje mayor a 1000 chars → BusinessException 400")
        void mensajeMuyLargo() {
            Notification n = buildNotification("EMAIL", "a@b.com");
            n.setMessage("X".repeat(1001));
            assertThatThrownBy(() -> useCase.send(n))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("1000");
        }

        @Test
        @DisplayName("EMAIL sin customerContact → BusinessException 400")
        void emailSinContacto() {
            Notification n = buildNotification("EMAIL", null);
            assertThatThrownBy(() -> useCase.send(n))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("EMAIL");
        }

        @Test
        @DisplayName("EMAIL con formato inválido (@ausente) → BusinessException 400")
        void emailFormatoInvalido() {
            Notification n = buildNotification("EMAIL", "correo-sin-arroba.com");
            assertThatThrownBy(() -> useCase.send(n))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("formato válido");
        }

        @Test
        @DisplayName("SMS sin customerContact → BusinessException 400")
        void smsSinContacto() {
            Notification n = buildNotification("SMS", "");
            assertThatThrownBy(() -> useCase.send(n))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("SMS");
        }

        @Test
        @DisplayName("Tipo nulo → BusinessException 400")
        void tipoNulo() {
            Notification n = buildNotification("EMAIL", "a@b.com");
            n.setType(null);
            assertThatThrownBy(() -> useCase.send(n))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("tipo");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENVÍO INDIVIDUAL — fallo del dispatcher
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("send() — fallos del dispatcher")
    class SendFalloDispatcher {

        @Test
        @DisplayName("Dispatcher lanza RuntimeException → BusinessException 502")
        void dispatcherLanzaException() {
            when(dispatcher.dispatch(anyString(), anyString(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("Twilio error"));

            Notification n = buildNotification("SMS", "+573001234567");
            assertThatThrownBy(() -> useCase.send(n))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getHttpStatus())
                    .isEqualTo(502);
        }

        @Test
        @DisplayName("Dispatcher lanza BusinessException → se re-lanza sin envolver")
        void dispatcherLanzaBusinessException() {
            BusinessException original = new BusinessException("Canal no soportado", 400);
            when(dispatcher.dispatch(anyString(), anyString(), anyString(), anyString()))
                    .thenThrow(original);

            Notification n = buildNotification("SMS", "+573001234567");
            assertThatThrownBy(() -> useCase.send(n))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Canal no soportado")
                    .extracting(e -> ((BusinessException) e).getHttpStatus())
                    .isEqualTo(400);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENVÍO MASIVO
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("sendBulk() — pruebas")
    class SendBulk {

        @Test
        @DisplayName("Lista nula → BusinessException 400")
        void listaNula() {
            assertThatThrownBy(() -> useCase.sendBulk(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("vacía");
        }

        @Test
        @DisplayName("Lista vacía → BusinessException 400")
        void listaVacia() {
            assertThatThrownBy(() -> useCase.sendBulk(Collections.emptyList()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("vacía");
        }

        @Test
        @DisplayName("Lista con más de 200 elementos → BusinessException 400")
        void listaMayorAlLimite() {
            List<Notification> lista = IntStream.range(0, 201)
                    .mapToObj(i -> buildNotification("PUSH", "token-" + i))
                    .toList();
            assertThatThrownBy(() -> useCase.sendBulk(lista))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("200");
        }

        @Test
        @DisplayName("Envío masivo exitoso → todos con status SENT")
        void envioMasivoTodosExitosos() {
            when(dispatcher.dispatch(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn("PUSH_SIMULATED");

            List<Notification> lista = List.of(
                    buildNotification("PUSH", "token-1"),
                    buildNotification("PUSH", "token-2"),
                    buildNotification("PUSH", "token-3")
            );

            List<Notification> results = useCase.sendBulk(lista);

            assertThat(results).hasSize(3);
            assertThat(results).allMatch(n -> "SENT".equals(n.getStatus()));
        }

        @Test
        @DisplayName("Envío masivo parcial → mezcla SENT y FAILED")
        void envioMasivoParcialConFallos() {
            when(dispatcher.dispatch(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn("OK")                           // primera llamada OK
                    .thenThrow(new RuntimeException("error"))  // segunda falla
                    .thenReturn("OK");                         // tercera OK

            List<Notification> lista = List.of(
                    buildNotification("PUSH", "token-1"),
                    buildNotification("PUSH", "token-2"),
                    buildNotification("PUSH", "token-3")
            );

            List<Notification> results = useCase.sendBulk(lista);

            assertThat(results).hasSize(3);
            long sent   = results.stream().filter(n -> "SENT".equals(n.getStatus())).count();
            long failed = results.stream().filter(n -> "FAILED".equals(n.getStatus())).count();
            assertThat(sent).isEqualTo(2);
            assertThat(failed).isEqualTo(1);
        }

        @Test
        @DisplayName("Exactamente 200 notificaciones → permitido")
        void listaEnElLimiteMaximo() {
            when(dispatcher.dispatch(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn("PUSH_SIMULATED");

            List<Notification> lista = IntStream.range(0, 200)
                    .mapToObj(i -> buildNotification("PUSH", "token-" + i))
                    .toList();

            assertThatCode(() -> useCase.sendBulk(lista)).doesNotThrowAnyException();
        }
    }
}
