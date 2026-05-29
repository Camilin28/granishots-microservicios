package com.granishots.login.infraestructure.mapper;

import com.granishots.login.application.dto.RegisterRequestDTO;
import com.granishots.login.application.dto.UserResponseDTO;
import com.granishots.login.domain.model.User;
import com.granishots.login.infraestructure.driver_adapters.jpa_repository.UserData;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserData toUserData(User u) {
        return new UserData(u.getId(), u.getName(), u.getEmail(),
                u.getPassword(), u.getRole(), u.getActive());
    }

    public User toUser(UserData d) {
        return new User(d.getId(), d.getName(), d.getEmail(),
                d.getPassword(), d.getRole(), d.getActive());
    }

    public User toUserFromRegisterDTO(RegisterRequestDTO dto) {
        return new User(null, dto.getName(), dto.getEmail(),
                dto.getPassword(), dto.getRole(), true);
    }

    public UserResponseDTO toUserResponseDTO(User u) {
        // Nunca exponer la contraseña en la respuesta
        return new UserResponseDTO(u.getId(), u.getName(),
                u.getEmail(), u.getRole(), u.getActive());
    }
}
