package com.granishots.login.domain.usecase;

import com.granishots.login.domain.exception.BusinessException;
import com.granishots.login.domain.model.User;
import com.granishots.login.domain.model.gateway.EncrypterGateway;
import com.granishots.login.domain.model.gateway.UserGateway;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class UserUseCase {

    private final UserGateway userGateway;
    private final EncrypterGateway encrypterGateway;

    private static final List<String> ROLES_VALIDOS = List.of("ADMIN", "EMPLOYEE", "CLIENT");
    private static final int NOMBRE_MAX = 100;
    private static final int PASSWORD_MIN = 6;
    private static final int PASSWORD_MAX = 100;

    // ── Registro ────────────────────────────────────────────────────────────

    public User register(User user) {
        validarUsuario(user, true);
        if (userGateway.findByEmail(user.getEmail().trim().toLowerCase()).isPresent())
            throw new BusinessException("Ya existe un usuario registrado con el email: " + user.getEmail().trim(), 409);
        user.setName(user.getName().trim());
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setRole(user.getRole().trim().toUpperCase());
        user.setPassword(encrypterGateway.encrypt(user.getPassword()));
        user.setActive(true);
        return userGateway.save(user);
    }

    // ── Login ────────────────────────────────────────────────────────────────

    public LoginResult login(String email, String rawPassword) {
        if (email == null || email.trim().isEmpty())
            throw new BusinessException("El email es obligatorio", 400);
        if (rawPassword == null || rawPassword.trim().isEmpty())
            throw new BusinessException("La contraseña es obligatoria", 400);
        if (!email.trim().contains("@"))
            throw new BusinessException("El formato del email no es válido", 400);

        User user = userGateway.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("Credenciales incorrectas", 401));

        if (!Boolean.TRUE.equals(user.getActive()))
            throw new BusinessException("El usuario está inactivo. Contacta al administrador.", 403);

        boolean passwordMatch = encrypterGateway.matches(rawPassword, user.getPassword());

        if (!passwordMatch)
            throw new BusinessException("Credenciales incorrectas", 401);

        return new LoginResult(true, "Inicio de sesión exitoso", user.getId(),
                user.getName(), user.getEmail(), user.getRole());
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public User findById(Long id) {
        validarId(id);
        return userGateway.findById(id)
                .orElseThrow(() -> new BusinessException("No existe un usuario con el id: " + id, 404));
    }

    public List<User> findAll() { return userGateway.findAll(); }

    public User update(Long id, User user) {
        validarId(id);
        validarUsuario(user, false);
        User existente = userGateway.findById(id)
                .orElseThrow(() -> new BusinessException("No existe un usuario con el id: " + id, 404));
        boolean emailDuplicado = userGateway.findByEmail(user.getEmail().trim().toLowerCase())
                .map(u -> !u.getId().equals(id)).orElse(false);
        if (emailDuplicado)
            throw new BusinessException("Ya existe otro usuario con el email: " + user.getEmail().trim(), 409);
        user.setId(id);
        user.setName(user.getName().trim());
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setRole(user.getRole().trim().toUpperCase());
        user.setActive(existente.getActive());
        // Si viene nueva contraseña la encripta, si no conserva la actual
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            validarPassword(user.getPassword());
            user.setPassword(encrypterGateway.encrypt(user.getPassword()));
        } else {
            user.setPassword(existente.getPassword());
        }
        return userGateway.update(user);
    }

    public void deleteById(Long id) {
        validarId(id);
        userGateway.findById(id)
                .orElseThrow(() -> new BusinessException("No existe un usuario con el id: " + id, 404));
        userGateway.deleteById(id);
    }

    public User updateStatus(Long id, Boolean active) {
        validarId(id);
        if (active == null) throw new BusinessException("El campo 'active' no puede ser nulo", 400);
        User user = userGateway.findById(id)
                .orElseThrow(() -> new BusinessException("No existe un usuario con el id: " + id, 404));
        if (user.getActive().equals(active))
            throw new BusinessException("El usuario ya está " + (active ? "activo" : "inactivo"), 400);
        return userGateway.updateStatus(id, active);
    }

    public boolean checkPassword(Long id, String rawPassword) {
        validarId(id);
        if (rawPassword == null || rawPassword.trim().isEmpty())
            throw new BusinessException("La contraseña no puede estar vacía", 400);
        User user = userGateway.findById(id)
                .orElseThrow(() -> new BusinessException("No existe un usuario con el id: " + id, 404));
        return encrypterGateway.matches(rawPassword, user.getPassword());
    }

    // ── Validaciones privadas ────────────────────────────────────────────────

    private void validarId(Long id) {
        if (id == null) throw new BusinessException("El ID no puede ser nulo", 400);
        if (id <= 0) throw new BusinessException("El ID debe ser positivo. Recibido: " + id, 400);
    }

    private void validarPassword(String password) {
        if (password.length() < PASSWORD_MIN)
            throw new BusinessException("La contraseña debe tener al menos " + PASSWORD_MIN + " caracteres", 400);
        if (password.length() > PASSWORD_MAX)
            throw new BusinessException("La contraseña no puede superar " + PASSWORD_MAX + " caracteres", 400);
    }

    private void validarUsuario(User user, boolean esRegistro) {
        if (user == null) throw new BusinessException("El cuerpo del usuario no puede ser nulo", 400);
        if (user.getName() == null || user.getName().trim().isEmpty())
            throw new BusinessException("El nombre es obligatorio", 400);
        if (user.getName().trim().length() > NOMBRE_MAX)
            throw new BusinessException("El nombre no puede superar " + NOMBRE_MAX + " caracteres", 400);
        if (user.getEmail() == null || user.getEmail().trim().isEmpty())
            throw new BusinessException("El email es obligatorio", 400);
        if (!user.getEmail().trim().contains("@") || !user.getEmail().trim().contains("."))
            throw new BusinessException("El formato del email no es válido: '" + user.getEmail() + "'", 400);
        if (user.getRole() == null || user.getRole().trim().isEmpty())
            throw new BusinessException("El rol es obligatorio", 400);
        if (!ROLES_VALIDOS.contains(user.getRole().trim().toUpperCase()))
            throw new BusinessException("Rol inválido: '" + user.getRole() + "'. Válidos: " + ROLES_VALIDOS, 400);
        if (esRegistro) {
            if (user.getPassword() == null || user.getPassword().trim().isEmpty())
                throw new BusinessException("La contraseña es obligatoria en el registro", 400);
            validarPassword(user.getPassword());
        }
    }

    // ── Record resultado login ───────────────────────────────────────────────
    public record LoginResult(
            boolean success,
            String mensaje,
            Long userId,
            String name,
            String email,
            String role) {}
}
