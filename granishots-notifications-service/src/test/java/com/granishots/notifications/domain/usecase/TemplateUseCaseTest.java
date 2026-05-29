package com.granishots.notifications.domain.usecase;

import com.granishots.notifications.domain.exception.BusinessException;
import com.granishots.notifications.domain.model.NotificationTemplate;
import com.granishots.notifications.domain.model.gateway.TemplateGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TemplateUseCase — Pruebas Unitarias")
class TemplateUseCaseTest {

    @Mock
    private TemplateGateway templateGateway;

    private TemplateUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new TemplateUseCase(templateGateway);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private NotificationTemplate buildTemplate() {
        return new NotificationTemplate(null, "Bienvenida", "WELCOME", "EMAIL",
                "Bienvenido a GraniShots", "Hola {{customerName}}, bienvenido!", true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // save()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("save() — guardar plantilla")
    class Save {

        @Test
        @DisplayName("Plantilla válida → guardada y retornada con active=true")
        void guardarPlantillaValida() {
            NotificationTemplate saved = buildTemplate();
            saved.setId(1L);
            when(templateGateway.save(any())).thenReturn(saved);

            NotificationTemplate result = useCase.save(buildTemplate());

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getActive()).isTrue();
            verify(templateGateway).save(any(NotificationTemplate.class));
        }

        @Test
        @DisplayName("Canal y tipo en minúsculas → normalizados a mayúsculas")
        void normalizacionMayusculas() {
            NotificationTemplate t = buildTemplate();
            t.setType("welcome");
            t.setChannel("email");
            when(templateGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

            NotificationTemplate result = useCase.save(t);

            assertThat(result.getType()).isEqualTo("WELCOME");
            assertThat(result.getChannel()).isEqualTo("EMAIL");
        }

        @Test
        @DisplayName("Nombre nulo → BusinessException 400")
        void nombreNulo() {
            NotificationTemplate t = buildTemplate();
            t.setName(null);
            assertThatThrownBy(() -> useCase.save(t))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nombre");
        }

        @Test
        @DisplayName("Nombre vacío → BusinessException 400")
        void nombreVacio() {
            NotificationTemplate t = buildTemplate();
            t.setName("  ");
            assertThatThrownBy(() -> useCase.save(t))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Nombre mayor a 100 chars → BusinessException 400")
        void nombreMuyLargo() {
            NotificationTemplate t = buildTemplate();
            t.setName("A".repeat(101));
            assertThatThrownBy(() -> useCase.save(t))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("100");
        }

        @Test
        @DisplayName("Tipo inválido → BusinessException 400")
        void tipoInvalido() {
            NotificationTemplate t = buildTemplate();
            t.setType("OTRO");
            assertThatThrownBy(() -> useCase.save(t))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("OTRO");
        }

        @Test
        @DisplayName("Canal inválido → BusinessException 400")
        void canalInvalido() {
            NotificationTemplate t = buildTemplate();
            t.setChannel("FAX");
            assertThatThrownBy(() -> useCase.save(t))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("FAX");
        }

        @Test
        @DisplayName("Body vacío → BusinessException 400")
        void bodyVacio() {
            NotificationTemplate t = buildTemplate();
            t.setBody("  ");
            assertThatThrownBy(() -> useCase.save(t))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cuerpo");
        }

        @Test
        @DisplayName("Body mayor a 2000 chars → BusinessException 400")
        void bodyMuyLargo() {
            NotificationTemplate t = buildTemplate();
            t.setBody("X".repeat(2001));
            assertThatThrownBy(() -> useCase.save(t))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("2000");
        }

        @Test
        @DisplayName("Plantilla nula → BusinessException 400")
        void plantillaNula() {
            assertThatThrownBy(() -> useCase.save(null))
                    .isInstanceOf(BusinessException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findById()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById() — buscar por ID")
    class FindById {

        @Test
        @DisplayName("ID existente → retorna plantilla")
        void encontrarPlantilla() {
            NotificationTemplate t = buildTemplate();
            t.setId(1L);
            when(templateGateway.findById(1L)).thenReturn(t);

            NotificationTemplate result = useCase.findById(1L);
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("ID inexistente → BusinessException 404")
        void noEncontrado() {
            when(templateGateway.findById(99L)).thenReturn(null);
            assertThatThrownBy(() -> useCase.findById(99L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getHttpStatus())
                    .isEqualTo(404);
        }

        @Test
        @DisplayName("ID nulo → BusinessException 400")
        void idNulo() {
            assertThatThrownBy(() -> useCase.findById(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nulo");
        }

        @Test
        @DisplayName("ID negativo → BusinessException 400")
        void idNegativo() {
            assertThatThrownBy(() -> useCase.findById(-5L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("positivo");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findAll()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findAll() — listar plantillas")
    class FindAll {

        @Test
        @DisplayName("Retorna todas las plantillas")
        void listarTodas() {
            List<NotificationTemplate> lista = List.of(buildTemplate(), buildTemplate());
            when(templateGateway.findAll()).thenReturn(lista);

            assertThat(useCase.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("Sin plantillas → lista vacía")
        void listaVacia() {
            when(templateGateway.findAll()).thenReturn(List.of());
            assertThat(useCase.findAll()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByType()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByType() — buscar por tipo")
    class FindByType {

        @Test
        @DisplayName("Tipo válido → delega al gateway")
        void tipoValido() {
            when(templateGateway.findByType("WELCOME")).thenReturn(List.of(buildTemplate()));
            List<NotificationTemplate> result = useCase.findByType("welcome");
            assertThat(result).hasSize(1);
            verify(templateGateway).findByType("WELCOME");
        }

        @Test
        @DisplayName("Tipo inválido → BusinessException 400")
        void tipoInvalido() {
            assertThatThrownBy(() -> useCase.findByType("OTRO"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("OTRO");
        }

        @Test
        @DisplayName("Tipo nulo → BusinessException 400")
        void tipoNulo() {
            assertThatThrownBy(() -> useCase.findByType(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("vacío");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByChannel()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByChannel() — buscar por canal")
    class FindByChannel {

        @Test
        @DisplayName("Canal válido → delega al gateway")
        void canalValido() {
            when(templateGateway.findByChannel("SMS")).thenReturn(List.of(buildTemplate()));
            List<NotificationTemplate> result = useCase.findByChannel("sms");
            assertThat(result).hasSize(1);
            verify(templateGateway).findByChannel("SMS");
        }

        @Test
        @DisplayName("Canal inválido → BusinessException 400")
        void canalInvalido() {
            assertThatThrownBy(() -> useCase.findByChannel("TELEX"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("TELEX");
        }

        @Test
        @DisplayName("Canal vacío → BusinessException 400")
        void canalVacio() {
            assertThatThrownBy(() -> useCase.findByChannel("  "))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("vacío");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // update()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update() — actualizar plantilla")
    class Update {

        @Test
        @DisplayName("Actualización exitosa → plantilla actualizada")
        void actualizarExitoso() {
            NotificationTemplate existing = buildTemplate();
            existing.setId(1L);
            when(templateGateway.findById(1L)).thenReturn(existing);
            when(templateGateway.update(any())).thenAnswer(inv -> inv.getArgument(0));

            NotificationTemplate updated = buildTemplate();
            updated.setName("Bienvenida V2");
            NotificationTemplate result = useCase.update(1L, updated);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Bienvenida V2");
        }

        @Test
        @DisplayName("ID inexistente → BusinessException 404")
        void idInexistente() {
            when(templateGateway.findById(99L)).thenReturn(null);
            assertThatThrownBy(() -> useCase.update(99L, buildTemplate()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getHttpStatus())
                    .isEqualTo(404);
        }

        @Test
        @DisplayName("ID cero → BusinessException 400")
        void idCero() {
            assertThatThrownBy(() -> useCase.update(0L, buildTemplate()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("positivo");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteById()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteById() — eliminar plantilla")
    class DeleteById {

        @Test
        @DisplayName("ID existente → eliminado sin excepción")
        void eliminarExitoso() {
            NotificationTemplate t = buildTemplate();
            t.setId(1L);
            when(templateGateway.findById(1L)).thenReturn(t);
            doNothing().when(templateGateway).deleteById(1L);

            assertThatCode(() -> useCase.deleteById(1L)).doesNotThrowAnyException();
            verify(templateGateway).deleteById(1L);
        }

        @Test
        @DisplayName("ID inexistente → BusinessException 404")
        void eliminarInexistente() {
            when(templateGateway.findById(50L)).thenReturn(null);
            assertThatThrownBy(() -> useCase.deleteById(50L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getHttpStatus())
                    .isEqualTo(404);
            verify(templateGateway, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("ID nulo → BusinessException 400")
        void idNulo() {
            assertThatThrownBy(() -> useCase.deleteById(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nulo");
        }
    }
}
