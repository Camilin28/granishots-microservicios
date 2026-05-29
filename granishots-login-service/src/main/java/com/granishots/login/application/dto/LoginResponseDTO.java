package com.granishots.login.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class LoginResponseDTO {
    private boolean success;
    private String mensaje;
    private Long userId;
    private String name;
    private String email;
    private String role;
}
