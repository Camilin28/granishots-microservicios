package com.granishots.notifications.infraestructure.entry_points;

import com.granishots.notifications.application.dto.ApiResponse;
import com.granishots.notifications.application.dto.TemplateRequestDTO;
import com.granishots.notifications.application.dto.TemplateResponseDTO;
import com.granishots.notifications.domain.usecase.TemplateUseCase;
import com.granishots.notifications.infraestructure.mapper.NotificationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateUseCase templateUseCase;
    private final NotificationMapper notificationMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<TemplateResponseDTO>> save(
            @Valid @RequestBody TemplateRequestDTO dto) {
        return new ResponseEntity<>(
                ApiResponse.created("Plantilla creada exitosamente",
                        notificationMapper.toTemplateResponseDTO(
                                templateUseCase.save(notificationMapper.toTemplateFromDTO(dto)))),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateResponseDTO>>> findAll() {
        List<TemplateResponseDTO> list = templateUseCase.findAll().stream()
                .map(notificationMapper::toTemplateResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Plantillas obtenidas", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponseDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Plantilla encontrada",
                notificationMapper.toTemplateResponseDTO(templateUseCase.findById(id))));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<TemplateResponseDTO>>> findByType(@PathVariable String type) {
        List<TemplateResponseDTO> list = templateUseCase.findByType(type).stream()
                .map(notificationMapper::toTemplateResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Plantillas por tipo: " + type, list));
    }

    @GetMapping("/channel/{channel}")
    public ResponseEntity<ApiResponse<List<TemplateResponseDTO>>> findByChannel(@PathVariable String channel) {
        List<TemplateResponseDTO> list = templateUseCase.findByChannel(channel).stream()
                .map(notificationMapper::toTemplateResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Plantillas por canal: " + channel, list));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponseDTO>> update(
            @PathVariable Long id, @Valid @RequestBody TemplateRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok("Plantilla actualizada exitosamente",
                notificationMapper.toTemplateResponseDTO(
                        templateUseCase.update(id, notificationMapper.toTemplateFromDTO(dto)))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        templateUseCase.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Plantilla " + id + " eliminada correctamente", null));
    }
}
