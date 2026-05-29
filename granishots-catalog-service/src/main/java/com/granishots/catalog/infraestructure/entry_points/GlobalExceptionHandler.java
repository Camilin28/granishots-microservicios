package com.granishots.catalog.infraestructure.entry_points;

import com.granishots.catalog.application.dto.ApiResponse;
import com.granishots.catalog.domain.exception.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Errores de validación @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                campos.put(error.getField(),
                        error.getDefaultMessage() + " — recibido: " + error.getRejectedValue()));
        return ResponseEntity.badRequest()
                .body(buildError(400, "Error de validación en los campos", campos));
    }

    // 2. Excepciones de negocio con código HTTP personalizado
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getHttpStatus());
        if (status == null) status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(buildError(ex.getHttpStatus(), ex.getMessage(), null));
    }

    // 3. JSON mal formado o tipo incorrecto
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleJsonErrors(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(buildError(400, "El body JSON es inválido o está mal formado. Verifica la sintaxis.", null));
    }

    // 4. Tipo de parámetro incorrecto en la URL (ej: /products/abc en lugar de /products/1)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String mensaje = "El parámetro '" + ex.getName() + "' debe ser de tipo "
                + ex.getRequiredType().getSimpleName() + ". Recibido: '" + ex.getValue() + "'";
        return ResponseEntity.badRequest().body(buildError(400, mensaje, null));
    }

    // 5. Parámetro requerido en query string ausente
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(buildError(400, "Parámetro requerido ausente: '" + ex.getParameterName() + "'", null));
    }

    // 6. URL no encontrada
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(404, "Ruta no encontrada: " + ex.getResourcePath(), null));
    }

    // 7. Método HTTP no permitido
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(buildError(405, "Método '" + ex.getMethod() + "' no permitido en esta ruta. "
                        + "Métodos soportados: " + ex.getSupportedHttpMethods(), null));
    }

    // 8. Violación de restricción de base de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.badRequest()
                .body(buildError(400, "Violación de restricción en la base de datos — "
                        + "posible dato duplicado o referencia inválida", null));
    }

    // 9. Número fuera de rango o formato inválido
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ApiResponse<Void>> handleNumberFormat(NumberFormatException ex) {
        return ResponseEntity.badRequest()
                .body(buildError(400, "Formato numérico inválido: " + ex.getMessage(), null));
    }

    // 10. Captura total — NUNCA debe llegar aquí un 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralErrors(Exception ex) {
        // Loguear para debugging, pero respuesta controlada
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(500,
                        "Error interno inesperado. Por favor contacta al administrador. Detalle: "
                        + ex.getClass().getSimpleName(), null));
    }

    private <T> ApiResponse<T> buildError(int status, String mensaje, T detalle) {
        return new ApiResponse<>(status, mensaje, detalle, LocalDateTime.now().toString());
    }
}
