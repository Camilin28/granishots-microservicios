package com.granishots.login.infraestructure.entry_points;

import com.granishots.login.application.dto.*;
import com.granishots.login.domain.exception.BusinessException;
import com.granishots.login.domain.usecase.UserUseCase;
import com.granishots.login.infraestructure.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;
    private final UserMapper userMapper;

    // ── Registro ────────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO dto) {
        var user = userUseCase.register(userMapper.toUserFromRegisterDTO(dto));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Usuario registrado exitosamente",
                        userMapper.toUserResponseDTO(user)));
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        UserUseCase.LoginResult result = userUseCase.login(dto.getEmail(), dto.getPassword());
        LoginResponseDTO response = new LoginResponseDTO(
                result.success(), result.mensaje(), result.userId(),
                result.name(), result.email(), result.role());
        return ResponseEntity.ok(ApiResponse.ok("Autenticación exitosa", response));
    }

    // ── Verificar contraseña (retorna boolean) ───────────────────────────────

    @PostMapping("/{id}/check-password")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkPassword(
            @PathVariable Long id,
            @Valid @RequestBody CheckPasswordRequestDTO dto) {
        boolean matches = userUseCase.checkPassword(id, dto.getPassword());
        Map<String, Boolean> result = Map.of("matches", matches);
        String mensaje = matches ? "La contraseña es correcta" : "La contraseña no coincide";
        return ResponseEntity.ok(ApiResponse.ok(mensaje, result));
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> findAll() {
        List<UserResponseDTO> list = userUseCase.findAll().stream()
                .map(userMapper::toUserResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Usuarios obtenidos — total: " + list.size(), list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario encontrado",
                userMapper.toUserResponseDTO(userUseCase.findById(id))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody RegisterRequestDTO dto) {
        var updated = userUseCase.update(id, userMapper.toUserFromRegisterDTO(dto));
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado exitosamente",
                userMapper.toUserResponseDTO(updated)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        if (!body.containsKey("active"))
            throw new BusinessException("El campo 'active' es obligatorio", 400);
        Object val = body.get("active");
        if (!(val instanceof Boolean))
            throw new BusinessException("El campo 'active' debe ser true o false", 400);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado",
                userMapper.toUserResponseDTO(userUseCase.updateStatus(id, (Boolean) val))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        userUseCase.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario " + id + " eliminado correctamente", null));
    }
    @RestController
    public class HealthController {

        @GetMapping("/health")
        public String health() {
            return "OK";
        }
    }
}
