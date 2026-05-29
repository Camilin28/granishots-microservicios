package com.granishots.login.domain.usecase;

import com.granishots.login.domain.exception.BusinessException;
import com.granishots.login.domain.model.User;
import com.granishots.login.domain.model.gateway.EncrypterGateway;
import com.granishots.login.domain.model.gateway.UserGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserUseCase — Pruebas unitarias")
class UserUseCaseTest {
    @Mock
    private UserGateway userGateway;
    @Mock
    private EncrypterGateway encrypterGateway;
    @InjectMocks
    private UserUseCase userUseCase;
    private User usuarioValido;
    @BeforeEach
    void setUp() {
        usuarioValido = new User(null, "María López", "maria@granishots.com", "password123", "EMPLOYEE", null);
    }

    @Nested
    @DisplayName("register()")
    class Register {
        @Test
        @DisplayName("Debe registrar un usuario válido, encriptar la contraseña y marcarlo activo")
        void debeRegistrarUsuarioValido() {
            User registrado = new User(1L, "María López", "maria@granishots.com", "$2a$10$hash", "EMPLOYEE", true);
            when(userGateway.findByEmail("maria@granishots.com")).thenReturn(Optional.empty());
            when(encrypterGateway.encrypt("password123")).thenReturn("$2a$10$hash");
            when(userGateway.save(any())).thenReturn(registrado);
            User resultado = userUseCase.register(usuarioValido);
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getActive()).isTrue();
            verify(encrypterGateway).encrypt("password123");
            verify(userGateway).save(any());
        }
        @Test
        @DisplayName("Debe lanzar excepción 409 si el email ya existe")
        void debeLanzarExcepcionEmailDuplicado() {
            when(userGateway.findByEmail("maria@granishots.com")).thenReturn(Optional.of(usuarioValido));
            assertThatThrownBy(() -> userUseCase.register(usuarioValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Ya existe un usuario registrado con el email");
        }
        @Test
        @DisplayName("Debe normalizar email a minúsculas")
        void debeNormalizarEmail() {
            usuarioValido.setEmail("MARIA@GRANISHOTS.COM");
            when(userGateway.findByEmail("maria@granishots.com")).thenReturn(Optional.empty());
            when(encrypterGateway.encrypt(any())).thenReturn("hash");
            when(userGateway.save(any())).thenAnswer(inv -> {User u = inv.getArgument(0);
                assertThat(u.getEmail()).isEqualTo("maria@granishots.com");
                return u;
            });
            userUseCase.register(usuarioValido);
        }
        @Test
        @DisplayName("Debe lanzar excepción si el email no tiene @")
        void debeLanzarExcepcionEmailSinArroba() {
            usuarioValido.setEmail("mariagramail.com");
            assertThatThrownBy(() -> userUseCase.register(usuarioValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("formato del email no es válido");
        }
        @Test
        @DisplayName("Debe lanzar excepción si la contraseña tiene menos de 6 caracteres")
        void debeLanzarExcepcionPasswordCorta() {
            usuarioValido.setPassword("abc");
            assertThatThrownBy(() -> userUseCase.register(usuarioValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("al menos 6 caracteres");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el rol es inválido")
        void debeLanzarExcepcionRolInvalido() {
            usuarioValido.setRole("SUPERADMIN");
            assertThatThrownBy(() -> userUseCase.register(usuarioValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Rol inválido");
        }
        @Test
        @DisplayName("Debe aceptar todos los roles válidos")
        void debeAceptarRolesValidos() {
            for (String rol : List.of("ADMIN", "EMPLOYEE", "CLIENT")) {
                User u = new User(null, "Test", "test" + rol + "@x.com", "pass123", rol, null);
                when(userGateway.findByEmail(anyString())).thenReturn(Optional.empty());
                when(encrypterGateway.encrypt(any())).thenReturn("hash");
                when(userGateway.save(any())).thenReturn(u);
                assertThatCode(() -> userUseCase.register(u)).doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {
        @Test
        @DisplayName("Debe retornar LoginResult exitoso con credenciales correctas")
        void debeRetornarLoginExitoso() {
            User activo = new User(1L, "María", "maria@granishots.com", "$2a$hash", "EMPLOYEE", true);
            when(userGateway.findByEmail("maria@granishots.com")).thenReturn(Optional.of(activo));
            when(encrypterGateway.matches("password123", "$2a$hash")).thenReturn(true);
            UserUseCase.LoginResult resultado = userUseCase.login("maria@granishots.com", "password123");
            assertThat(resultado.success()).isTrue();
            assertThat(resultado.role()).isEqualTo("EMPLOYEE");
            assertThat(resultado.userId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Debe lanzar 401 si la contraseña es incorrecta")
        void debeLanzar401PasswordIncorrecta() {
            User activo = new User(1L, "María", "maria@granishots.com", "$2a$hash", "EMPLOYEE", true);
            when(userGateway.findByEmail("maria@granishots.com")).thenReturn(Optional.of(activo));
            when(encrypterGateway.matches("wrongpass", "$2a$hash")).thenReturn(false);

            assertThatThrownBy(() -> userUseCase.login("maria@granishots.com", "wrongpass"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Credenciales incorrectas");
        }

        @Test
        @DisplayName("Debe lanzar 401 si el usuario no existe")
        void debeLanzar401UsuarioNoExiste() {
            when(userGateway.findByEmail("noexiste@x.com")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> userUseCase.login("noexiste@x.com", "pass123"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Credenciales incorrectas");
        }
        @Test
        @DisplayName("Debe lanzar 403 si el usuario está inactivo")
        void debeLanzar403UsuarioInactivo() {
            User inactivo = new User(1L, "María", "maria@granishots.com", "$2a$hash", "EMPLOYEE", false);
            when(userGateway.findByEmail("maria@granishots.com")).thenReturn(Optional.of(inactivo));
            assertThatThrownBy(() -> userUseCase.login("maria@granishots.com", "password123"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("usuario está inactivo");
        }
        @Test
        @DisplayName("Debe lanzar excepción si la contraseña está vacía")
        void debeLanzarExcepcionPasswordVacia() {
            assertThatThrownBy(() -> userUseCase.login("user@x.com", ""))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("contraseña es obligatoria");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el email no tiene formato válido")
        void debeLanzarExcepcionFormatoEmail() {
            assertThatThrownBy(() -> userUseCase.login("noesunemail", "pass123"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("formato del email no es válido");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el nombre está vacío")
        void debeLanzarExcepcionNombreVacio() {
            usuarioValido.setName("  ");
            assertThatThrownBy(() -> userUseCase.register(usuarioValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nombre es obligatorio");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el nombre supera el máximo de caracteres")
        void debeLanzarExcepcionNombreLargo() {
            usuarioValido.setName("A".repeat(101));
            assertThatThrownBy(() -> userUseCase.register(usuarioValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nombre no puede superar");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el email está vacío")
        void debeLanzarExcepcionEmailVacio() {
            usuarioValido.setEmail("  ");
            assertThatThrownBy(() -> userUseCase.register(usuarioValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("email es obligatorio");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el rol está vacío")
        void debeLanzarExcepcionRolVacio() {
            usuarioValido.setRole("  ");
            assertThatThrownBy(() -> userUseCase.register(usuarioValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("rol es obligatorio");
        }
    }
    @Nested
    @DisplayName("findById()")
    class FindById {
        @Test
        @DisplayName("Debe retornar el usuario cuando existe")
        void debeRetornarUsuario() {
            User u = new User(1L, "María", "maria@x.com", "hash", "EMPLOYEE", true);
            when(userGateway.findById(1L)).thenReturn(Optional.of(u));
            User resultado = userUseCase.findById(1L);
            assertThat(resultado.getName()).isEqualTo("María");
        }
        @Test
        @DisplayName("Debe lanzar 404 si el usuario no existe")
        void debeLanzar404() {
            when(userGateway.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> userUseCase.findById(99L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un usuario con el id: 99");
        }
    }
    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("Debe cambiar el estado del usuario")
        void debeCambiarEstado() {
            User activo = new User(1L, "María", "maria@x.com", "hash", "EMPLOYEE", true);
            User inactivo = new User(1L, "María", "maria@x.com", "hash", "EMPLOYEE", false);
            when(userGateway.findById(1L)).thenReturn(Optional.of(activo));
            when(userGateway.updateStatus(1L, false)).thenReturn(inactivo);
            User resultado = userUseCase.updateStatus(1L, false);
            assertThat(resultado.getActive()).isFalse();
        }

        @Test
        @DisplayName("Debe lanzar excepción si el estado ya es el mismo")
        void debeLanzarExcepcionMismoEstado() {
            User activo = new User(1L, "María", "maria@x.com", "hash", "EMPLOYEE", true);
            when(userGateway.findById(1L)).thenReturn(Optional.of(activo));
            assertThatThrownBy(() -> userUseCase.updateStatus(1L, true))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ya está activo");
        }
        @Test
        @DisplayName("Debe actualizar un usuario correctamente conservando su estado active")
        void debeActualizarUsuario() {
            User existente = new User(1L, "María", "maria@x.com", "$2a$hash", "EMPLOYEE", true);
            User actualizado = new User(1L, "María López", "maria@x.com", "$2a$hash", "EMPLOYEE", true);
            when(userGateway.findById(1L)).thenReturn(Optional.of(existente));
            when(userGateway.findByEmail("maria@x.com")).thenReturn(Optional.of(existente));
            when(userGateway.update(any())).thenReturn(actualizado);
            User datos = new User(null, "María López", "maria@x.com", null, "EMPLOYEE", null);
            User resultado = userUseCase.update(1L, datos);
            assertThat(resultado.getName()).isEqualTo("María López");
            verify(userGateway).update(any());
        }
        @Test
        @DisplayName("Debe encriptar la nueva contraseña si viene en el update")
        void debeEncriptarNuevaPassword() {
            User existente = new User(1L, "María", "maria@x.com", "$2a$vieja", "EMPLOYEE", true);
            when(userGateway.findById(1L)).thenReturn(Optional.of(existente));
            when(userGateway.findByEmail("maria@x.com")).thenReturn(Optional.of(existente));
            when(encrypterGateway.encrypt("nueva123")).thenReturn("$2a$nueva");
            when(userGateway.update(any())).thenReturn(existente);
            User datos = new User(null, "María", "maria@x.com", "nueva123", "EMPLOYEE", null);
            userUseCase.update(1L, datos);
            verify(encrypterGateway).encrypt("nueva123");
        }

        @Test
        @DisplayName("Debe conservar la contraseña actual si no viene nueva en el update")
        void debeConservarPasswordActual() {
            User existente = new User(1L, "María", "maria@x.com", "$2a$vieja", "EMPLOYEE", true);
            when(userGateway.findById(1L)).thenReturn(Optional.of(existente));
            when(userGateway.findByEmail("maria@x.com")).thenReturn(Optional.of(existente));
            when(userGateway.update(any())).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                assertThat(u.getPassword()).isEqualTo("$2a$vieja");
                return u;
            });
            User datos = new User(null, "María", "maria@x.com", null, "EMPLOYEE", null);
            assertThatCode(() -> userUseCase.update(1L, datos)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debe lanzar 404 si el usuario a actualizar no existe")
        void debeLanzar404() {
            when(userGateway.findById(99L)).thenReturn(Optional.empty());
            User datos = new User(null, "Test", "test@x.com", null, "EMPLOYEE", null);
            assertThatThrownBy(() -> userUseCase.update(99L, datos))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un usuario con el id: 99");
        }
        @Test
        @DisplayName("Debe lanzar 409 si el nuevo email ya existe en otro usuario")
        void debeLanzar409EmailDuplicado() {
            User existente = new User(1L, "María", "maria@x.com", "$2a$hash", "EMPLOYEE", true);
            User otroConMismoEmail = new User(2L, "Pedro", "nuevo@x.com", "$2a$hash", "EMPLOYEE", true);
            when(userGateway.findById(1L)).thenReturn(Optional.of(existente));
            when(userGateway.findByEmail("nuevo@x.com")).thenReturn(Optional.of(otroConMismoEmail));
            User datos = new User(null, "María", "nuevo@x.com", null, "EMPLOYEE", null);
            assertThatThrownBy(() -> userUseCase.update(1L, datos))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Ya existe otro usuario con el email");
        }
    }
    @Nested
    @DisplayName("checkPassword()")
    class CheckPassword {
        @Test
        @DisplayName("Debe retornar true si la contraseña coincide")
        void debeRetornarTrue() {
            User u = new User(1L, "María", "maria@x.com", "$2a$hash", "EMPLOYEE", true);
            when(userGateway.findById(1L)).thenReturn(Optional.of(u));
            when(encrypterGateway.matches("pass123", "$2a$hash")).thenReturn(true);
            boolean resultado = userUseCase.checkPassword(1L, "pass123");
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si la contraseña no coincide")
        void debeRetornarFalse() {
            User u = new User(1L, "María", "maria@x.com", "$2a$hash", "EMPLOYEE", true);
            when(userGateway.findById(1L)).thenReturn(Optional.of(u));
            when(encrypterGateway.matches("wrongpass", "$2a$hash")).thenReturn(false);
            boolean resultado = userUseCase.checkPassword(1L, "wrongpass");
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Debe lanzar excepción si la contraseña está vacía")
        void debeLanzarExcepcionPasswordVacia() {
            assertThatThrownBy(() -> userUseCase.checkPassword(1L, "  "))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("contraseña no puede estar vacía");
        }
        @Test
        @DisplayName("Debe lanzar excepción si la contraseña es obligatoria en registro")
        void debeLanzarExcepcionPasswordObligatoria() {
            usuarioValido.setPassword(null);
            assertThatThrownBy(() -> userUseCase.register(usuarioValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("contraseña es obligatoria en el registro");
        }
        @Test
        @DisplayName("Debe lanzar excepción si la contraseña supera el máximo de caracteres")
        void debeLanzarExcepcionPasswordLarga() {
            usuarioValido.setPassword("A".repeat(101));
            assertThatThrownBy(() -> userUseCase.register(usuarioValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("contraseña no puede superar");
        }
    }
    @Nested
    @DisplayName("findAll()")
    class FindAll {
        @Test
        @DisplayName("Debe retornar todos los usuarios")
        void debeRetornarTodos() {
            when(userGateway.findAll()).thenReturn(List.of(usuarioValido, usuarioValido));
            List<User> resultado = userUseCase.findAll();
            assertThat(resultado).hasSize(2);
            verify(userGateway).findAll();
        }
    }
    @Nested
    @DisplayName("deleteById()")
    class DeleteById {
        @Test
        @DisplayName("Debe eliminar el usuario cuando existe")
        void debeEliminar() {
            when(userGateway.findById(1L)).thenReturn(Optional.of(usuarioValido));
            doNothing().when(userGateway).deleteById(1L);
            userUseCase.deleteById(1L);
            verify(userGateway).deleteById(1L);
        }
        @Test
        @DisplayName("Debe lanzar 404 si el usuario no existe")
        void debeLanzar404() {
            when(userGateway.findById(9L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> userUseCase.deleteById(9L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un usuario con el id: 9");
        }
    }
}