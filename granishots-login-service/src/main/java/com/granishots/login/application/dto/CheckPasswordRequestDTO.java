package com.granishots.login.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckPasswordRequestDTO {

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}
