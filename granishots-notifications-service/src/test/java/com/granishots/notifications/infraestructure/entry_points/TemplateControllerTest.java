package com.granishots.notifications.infraestructure.entry_points;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.granishots.notifications.application.dto.TemplateRequestDTO;
import com.granishots.notifications.application.dto.TemplateResponseDTO;
import com.granishots.notifications.domain.exception.BusinessException;
import com.granishots.notifications.domain.model.NotificationTemplate;
import com.granishots.notifications.domain.usecase.TemplateUseCase;
import com.granishots.notifications.infraestructure.mapper.NotificationMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TemplateController.class)
@DisplayName("TemplateController — Pruebas Web (MockMvc)")
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TemplateUseCase templateUseCase;

    @MockBean
    private NotificationMapper notificationMapper;

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private TemplateRequestDTO buildRequestDTO() {
        TemplateRequestDTO dto = new TemplateRequestDTO();
        dto.setName("Bienvenida");
        dto.setType("WELCOME");
        dto.setChannel("EMAIL");
        dto.setSubject("Bienvenido");
        dto.setBody("Hola {{customerName}}!");
        return dto;
    }

    private NotificationTemplate buildTemplate(Long id) {
        return new NotificationTemplate(id, "Bienvenida", "WELCOME", "EMAIL",
                "Bienvenido", "Hola {{customerName}}!", true);
    }

    private TemplateResponseDTO buildResponseDTO(Long id) {
        return new TemplateResponseDTO(id, "Bienvenida", "WELCOME", "EMAIL",
                "Bienvenido", "Hola {{customerName}}!", true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/templates
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /templates")
    class Create {

        @Test
        @DisplayName("Crear plantilla válida → 201 Created")
        void crearPlantillaValida() throws Exception {
            when(notificationMapper.toTemplateFromDTO(any())).thenReturn(buildTemplate(null));
            when(templateUseCase.save(any())).thenReturn(buildTemplate(1L));
            when(notificationMapper.toTemplateResponseDTO(any())).thenReturn(buildResponseDTO(1L));

            mockMvc.perform(post("/api/v1/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequestDTO())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("Nombre vacío en DTO → 400 Bad Request")
        void nombreVacio() throws Exception {
            TemplateRequestDTO dto = buildRequestDTO();
            dto.setName("");
            mockMvc.perform(post("/api/v1/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Tipo inválido en DTO → 400 Bad Request")
        void tipoInvalido() throws Exception {
            TemplateRequestDTO dto = buildRequestDTO();
            dto.setType("INVALIDO");
            mockMvc.perform(post("/api/v1/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Canal inválido en DTO → 400 Bad Request")
        void canalInvalido() throws Exception {
            TemplateRequestDTO dto = buildRequestDTO();
            dto.setChannel("FAX");
            mockMvc.perform(post("/api/v1/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/templates
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /templates")
    class FindAll {

        @Test
        @DisplayName("Listar todas las plantillas → 200 OK con lista")
        void listarTodas() throws Exception {
            when(templateUseCase.findAll()).thenReturn(List.of(buildTemplate(1L), buildTemplate(2L)));
            when(notificationMapper.toTemplateResponseDTO(any()))
                    .thenReturn(buildResponseDTO(1L), buildResponseDTO(2L));

            mockMvc.perform(get("/api/v1/templates"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("Sin plantillas → 200 OK con lista vacía")
        void listaVacia() throws Exception {
            when(templateUseCase.findAll()).thenReturn(List.of());
            mockMvc.perform(get("/api/v1/templates"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/templates/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /templates/{id}")
    class FindById {

        @Test
        @DisplayName("ID existente → 200 OK con plantilla")
        void encontrado() throws Exception {
            when(templateUseCase.findById(1L)).thenReturn(buildTemplate(1L));
            when(notificationMapper.toTemplateResponseDTO(any())).thenReturn(buildResponseDTO(1L));

            mockMvc.perform(get("/api/v1/templates/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("ID inexistente → 404 Not Found")
        void noEncontrado() throws Exception {
            when(templateUseCase.findById(99L)).thenThrow(new BusinessException("No existe", 404));

            mockMvc.perform(get("/api/v1/templates/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("ID no numérico → 400 Bad Request")
        void idNoNumerico() throws Exception {
            mockMvc.perform(get("/api/v1/templates/abc"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/templates/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /templates/{id}")
    class Update {

        @Test
        @DisplayName("Actualización exitosa → 200 OK")
        void actualizarExitoso() throws Exception {
            when(notificationMapper.toTemplateFromDTO(any())).thenReturn(buildTemplate(null));
            when(templateUseCase.update(eq(1L), any())).thenReturn(buildTemplate(1L));
            when(notificationMapper.toTemplateResponseDTO(any())).thenReturn(buildResponseDTO(1L));

            mockMvc.perform(put("/api/v1/templates/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequestDTO())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("ID inexistente en actualización → 404 Not Found")
        void idInexistente() throws Exception {
            when(notificationMapper.toTemplateFromDTO(any())).thenReturn(buildTemplate(null));
            when(templateUseCase.update(eq(99L), any()))
                    .thenThrow(new BusinessException("No existe", 404));

            mockMvc.perform(put("/api/v1/templates/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequestDTO())))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/templates/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /templates/{id}")
    class Delete {

        @Test
        @DisplayName("Eliminación exitosa → 200 OK con mensaje")
        void eliminarExitoso() throws Exception {
            doNothing().when(templateUseCase).deleteById(1L);

            mockMvc.perform(delete("/api/v1/templates/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensaje").value("Plantilla 1 eliminada correctamente"));
        }

        @Test
        @DisplayName("ID inexistente en eliminación → 404 Not Found")
        void eliminarInexistente() throws Exception {
            doThrow(new BusinessException("No existe", 404)).when(templateUseCase).deleteById(50L);

            mockMvc.perform(delete("/api/v1/templates/50"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/templates/type/{type}  y  /channel/{channel}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /type/WELCOME → 200 OK con plantillas filtradas")
    void findByType() throws Exception {
        when(templateUseCase.findByType("WELCOME")).thenReturn(List.of(buildTemplate(1L)));
        when(notificationMapper.toTemplateResponseDTO(any())).thenReturn(buildResponseDTO(1L));

        mockMvc.perform(get("/api/v1/templates/type/WELCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("GET /channel/EMAIL → 200 OK con plantillas filtradas")
    void findByChannel() throws Exception {
        when(templateUseCase.findByChannel("EMAIL")).thenReturn(List.of(buildTemplate(1L)));
        when(notificationMapper.toTemplateResponseDTO(any())).thenReturn(buildResponseDTO(1L));

        mockMvc.perform(get("/api/v1/templates/channel/EMAIL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }
}
