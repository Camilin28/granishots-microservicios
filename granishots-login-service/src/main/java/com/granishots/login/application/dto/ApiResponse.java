package com.granishots.login.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data @AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String mensaje;
    private T data;
    private String timestamp;

    public static <T> ApiResponse<T> ok(String mensaje, T data) {
        return new ApiResponse<>(200, mensaje, data, LocalDateTime.now().toString());
    }
    public static <T> ApiResponse<T> created(String mensaje, T data) {
        return new ApiResponse<>(201, mensaje, data, LocalDateTime.now().toString());
    }
}
