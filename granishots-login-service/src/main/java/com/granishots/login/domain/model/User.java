package com.granishots.login.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
    private String password;       // almacenada con BCrypt
    private String role;           // ADMIN, EMPLOYEE, CLIENT
    private Boolean active;
}
