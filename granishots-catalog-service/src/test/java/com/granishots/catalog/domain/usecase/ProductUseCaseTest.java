package com.granishots.catalog.domain.usecase;

import com.granishots.catalog.domain.exception.BusinessException;
import com.granishots.catalog.domain.model.Product;
import com.granishots.catalog.domain.model.gateway.ProductGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductUseCase — Pruebas unitarias")
class ProductUseCaseTest {

    @Mock
    private ProductGateway productGateway;

    @InjectMocks
    private ProductUseCase productUseCase;

    private Product productoValido;

    @BeforeEach
    void setUp() {
        productoValido = new Product(null, "Granizado de fresa", "Delicioso", 8000.0, "PEQUEÑA", "8", null);
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("Debe guardar un producto válido y marcarlo como disponible")
        void debeGuardarProductoValido() {
            Product guardado = new Product(1L, "Granizado de fresa", "Delicioso", 8000.0, "PEQUEÑA", "8", true);
            when(productGateway.findByName("Granizado de fresa")).thenReturn(List.of());
            when(productGateway.save(any())).thenReturn(guardado);

            Product resultado = productUseCase.save(productoValido);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getAvailable()).isTrue();
            verify(productGateway).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre ya existe")
        void debeLanzarExcepcionNombreDuplicado() {
            Product existente = new Product(1L, "Granizado de fresa", null, 8000.0, "PEQUEÑA", "8", true);
            when(productGateway.findByName("Granizado de fresa")).thenReturn(List.of(existente));

            assertThatThrownBy(() -> productUseCase.save(productoValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Ya existe un producto con el nombre");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el precio es cero o negativo")
        void debeLanzarExcepcionPrecioInvalido() {
            productoValido.setPrice(0.0);
            assertThatThrownBy(() -> productUseCase.save(productoValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("precio debe ser mayor a cero");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la categoría es inválida")
        void debeLanzarExcepcionCategoriaInvalida() {
            productoValido.setCategory("GRANDE");
            assertThatThrownBy(() -> productUseCase.save(productoValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Categoría inválida");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el tamaño es inválido")
        void debeLanzarExcepcionTamanioInvalido() {
            productoValido.setSize("32");
            assertThatThrownBy(() -> productUseCase.save(productoValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Tamaño inválido");
        }

        @Test
        @DisplayName("Debe aceptar todas las categorías válidas")
        void debeAceptarCategoriasValidas() {
            for (String cat : List.of("PEQUEÑA", "MEDIANO", "MEGA")) {
                Product p = new Product(null, "Producto " + cat, null, 5000.0, cat, "8", null);
                when(productGateway.findByName(anyString())).thenReturn(List.of());
                when(productGateway.save(any())).thenReturn(p);
                assertThatCode(() -> productUseCase.save(p)).doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("Debe aceptar todos los tamaños válidos (8, 16, 24)")
        void debeAceptarTamaniosValidos() {
            for (String size : List.of("8", "16", "24")) {
                Product p = new Product(null, "Prod " + size, null, 5000.0, "PEQUEÑA", size, null);
                when(productGateway.findByName(anyString())).thenReturn(List.of());
                when(productGateway.save(any())).thenReturn(p);
                assertThatCode(() -> productUseCase.save(p)).doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Debe retornar el producto cuando existe")
        void debeRetornarProducto() {
            Product p = new Product(1L, "Granizado", null, 8000.0, "PEQUEÑA", "8", true);
            when(productGateway.findById(1L)).thenReturn(p);

            Product resultado = productUseCase.findById(1L);

            assertThat(resultado.getName()).isEqualTo("Granizado");
        }

        @Test
        @DisplayName("Debe lanzar 404 si el producto no existe")
        void debeLanzar404() {
            when(productGateway.findById(99L)).thenReturn(null);

            assertThatThrownBy(() -> productUseCase.findById(99L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un producto con el id: 99");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el ID es cero")
        void debeLanzarExcepcionIdCero() {
            assertThatThrownBy(() -> productUseCase.findById(0L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ID debe ser positivo");
        }
    }
    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("Debe retornar todos los productos")
        void debeRetornarTodos() {
            when(productGateway.findAll()).thenReturn(List.of(productoValido, productoValido));

            List<Product> resultado = productUseCase.findAll();

            assertThat(resultado).hasSize(2);
            verify(productGateway).findAll();
        }
    }

    @Nested
    @DisplayName("findByCategory()")
    class FindByCategory {

        @Test
        @DisplayName("Debe retornar productos de la categoría indicada")
        void debeRetornarPorCategoria() {
            when(productGateway.findByCategory("PEQUEÑA")).thenReturn(List.of(productoValido));

            List<Product> resultado = productUseCase.findByCategory("pequeña");

            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Debe lanzar excepción con categoría inválida")
        void debeLanzarExcepcionCategoriaInvalida() {
            assertThatThrownBy(() -> productUseCase.findByCategory("GIGANTE"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Categoría inválida");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la categoría está vacía")
        void debeLanzarExcepcionCategoriaVacia() {
            assertThatThrownBy(() -> productUseCase.findByCategory(""))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("categoría no puede estar vacía");
        }
    }

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("Debe cambiar el estado de disponibilidad")
        void debeCambiarEstado() {
            Product activo = new Product(1L, "Granizado", null, 8000.0, "PEQUEÑA", "8", true);
            Product inactivo = new Product(1L, "Granizado", null, 8000.0, "PEQUEÑA", "8", false);
            when(productGateway.findById(1L)).thenReturn(activo);
            when(productGateway.updateStatus(1L, false)).thenReturn(inactivo);

            Product resultado = productUseCase.updateStatus(1L, false);

            assertThat(resultado.getAvailable()).isFalse();
        }

        @Test
        @DisplayName("Debe lanzar excepción si el estado ya es el mismo")
        void debeLanzarExcepcionMismoEstado() {
            Product activo = new Product(1L, "Granizado", null, 8000.0, "PEQUEÑA", "8", true);
            when(productGateway.findById(1L)).thenReturn(activo);

            assertThatThrownBy(() -> productUseCase.updateStatus(1L, true))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ya está activo");
        }

        @Test
        @DisplayName("Debe lanzar excepción si 'available' es nulo")
        void debeLanzarExcepcionAvailableNulo() {
            assertThatThrownBy(() -> productUseCase.updateStatus(1L, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("'available' no puede ser nulo");
        }
    }

    @Nested
    @DisplayName("updatePrice()")
    class UpdatePrice {

        @Test
        @DisplayName("Debe actualizar el precio correctamente")
        void debeActualizarPrecio() {
            Product p = new Product(1L, "Granizado", null, 8000.0, "PEQUEÑA", "8", true);
            Product actualizado = new Product(1L, "Granizado", null, 10000.0, "PEQUEÑA", "8", true);
            when(productGateway.findById(1L)).thenReturn(p);
            when(productGateway.updatePrice(1L, 10000.0)).thenReturn(actualizado);

            Product resultado = productUseCase.updatePrice(1L, 10000.0);

            assertThat(resultado.getPrice()).isEqualTo(10000.0);
        }

        @Test
        @DisplayName("Debe lanzar excepción si el precio es negativo")
        void debeLanzarExcepcionPrecioNegativo() {
            assertThatThrownBy(() -> productUseCase.updatePrice(1L, -100.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("precio debe ser mayor a cero");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el precio supera el máximo")
        void debeLanzarExcepcionPrecioMaximo() {
            assertThatThrownBy(() -> productUseCase.updatePrice(1L, 1_000_000.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no puede superar");
        }
    }
    @Nested
    @DisplayName("updatePrice() — producto no existe")
    class UpdatePrecioNoExiste {

        @Test
        @DisplayName("Debe lanzar 404 si el producto no existe al actualizar precio")
        void debeLanzar404() {
            when(productGateway.findById(99L)).thenReturn(null);

            assertThatThrownBy(() -> productUseCase.updatePrice(99L, 8000.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un producto con el id: 99");
        }
    }

    @Nested
    @DisplayName("deleteById()")
    class DeleteById {

        @Test
        @DisplayName("Debe eliminar el producto cuando existe")
        void debeEliminar() {
            when(productGateway.findById(1L)).thenReturn(productoValido);
            doNothing().when(productGateway).deleteById(1L);

            productUseCase.deleteById(1L);

            verify(productGateway).deleteById(1L);
        }

        @Test
        @DisplayName("Debe lanzar 404 si el producto no existe")
        void debeLanzar404() {
            when(productGateway.findById(7L)).thenReturn(null);

            assertThatThrownBy(() -> productUseCase.deleteById(7L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un producto con el id: 7");
        }
    }

    @Nested
    @DisplayName("findBySize()")
    class FindBySize {

        @Test
        @DisplayName("Debe retornar productos por tamaño válido")
        void debeRetornarPorTamanio() {
            when(productGateway.findBySize("8")).thenReturn(List.of(productoValido));

            List<Product> resultado = productUseCase.findBySize("8");

            assertThat(resultado).hasSize(1);
            verify(productGateway).findBySize("8");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el tamaño está vacío")
        void debeLanzarExcepcionVacio() {
            assertThatThrownBy(() -> productUseCase.findBySize("  "))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("tamaño no puede estar vacío");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el tamaño es inválido")
        void debeLanzarExcepcionInvalido() {
            assertThatThrownBy(() -> productUseCase.findBySize("32"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Tamaño inválido");
        }
    }

    @Nested
    @DisplayName("findAvailable()")
    class FindAvailable {

        @Test
        @DisplayName("Debe retornar productos disponibles")
        void debeRetornarDisponibles() {
            when(productGateway.findAvailable()).thenReturn(List.of(productoValido));

            List<Product> resultado = productUseCase.findAvailable();

            assertThat(resultado).hasSize(1);
            verify(productGateway).findAvailable();
        }
    }

    @Nested
    @DisplayName("findByName()")
    class FindByName {

        @Test
        @DisplayName("Debe retornar resultados con término válido")
        void debeRetornarResultados() {
            when(productGateway.findByName("gran")).thenReturn(List.of(productoValido));

            List<Product> resultado = productUseCase.findByName("gran");

            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Debe lanzar excepción si el término está vacío")
        void debeLanzarExcepcionVacio() {
            assertThatThrownBy(() -> productUseCase.findByName("  "))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("término de búsqueda no puede estar vacío");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el término tiene menos de 2 caracteres")
        void debeLanzarExcepcionMuyCorto() {
            assertThatThrownBy(() -> productUseCase.findByName("a"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("al menos 2 caracteres");
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Debe actualizar un producto existente correctamente")
        void debeActualizarProducto() {
            Product existente = new Product(1L, "Granizado fresa", null, 8000.0, "PEQUEÑA", "8", true);
            Product actualizado = new Product(1L, "Granizado mango", null, 9000.0, "PEQUEÑA", "8", true);
            when(productGateway.findById(1L)).thenReturn(existente);
            when(productGateway.findByName("Granizado mango")).thenReturn(List.of());
            when(productGateway.update(any())).thenReturn(actualizado);

            Product datos = new Product(null, "Granizado mango", null, 9000.0, "PEQUEÑA", "8", null);
            Product resultado = productUseCase.update(1L, datos);

            assertThat(resultado.getName()).isEqualTo("Granizado mango");
            verify(productGateway).update(any());
        }

        @Test
        @DisplayName("Debe lanzar 404 si el producto a actualizar no existe")
        void debeLanzar404() {
            when(productGateway.findById(99L)).thenReturn(null);

            Product datos = new Product(null, "Granizado", null, 8000.0, "PEQUEÑA", "8", null);
            assertThatThrownBy(() -> productUseCase.update(99L, datos))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un producto con el id: 99");
        }

        @Test
        @DisplayName("Debe lanzar 409 si el nuevo nombre ya existe en otro producto")
        void debeLanzar409NombreDuplicado() {
            Product existente = new Product(1L, "Granizado fresa", null, 8000.0, "PEQUEÑA", "8", true);
            Product otroCon = new Product(2L, "Granizado mango", null, 8000.0, "PEQUEÑA", "8", true);
            when(productGateway.findById(1L)).thenReturn(existente);
            when(productGateway.findByName("Granizado mango")).thenReturn(List.of(otroCon));

            Product datos = new Product(null, "Granizado mango", null, 8000.0, "PEQUEÑA", "8", null);
            assertThatThrownBy(() -> productUseCase.update(1L, datos))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Ya existe otro producto con el nombre");
        }

        @Test
        @DisplayName("Debe conservar el estado available del producto existente")
        void debeConservarAvailable() {
            Product existente = new Product(1L, "Granizado fresa", null, 8000.0, "PEQUEÑA", "8", false);
            when(productGateway.findById(1L)).thenReturn(existente);
            when(productGateway.findByName("Granizado fresa")).thenReturn(List.of(existente));
            when(productGateway.update(any())).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                assertThat(p.getAvailable()).isFalse();
                return p;
            });

            Product datos = new Product(null, "Granizado fresa", null, 8000.0, "PEQUEÑA", "8", null);
            assertThatCode(() -> productUseCase.update(1L, datos)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validarProducto() — casos límite")
    class ValidarProductoCasosLimite {

        @Test
        @DisplayName("Debe lanzar excepción si el precio es nulo")
        void debeLanzarExcepcionPrecioNulo() {
            productoValido.setPrice(null);
            assertThatThrownBy(() -> productUseCase.save(productoValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("precio es obligatorio");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el precio supera el máximo")
        void debeLanzarExcepcionPrecioMaximo() {
            productoValido.setPrice(1_000_000.0);
            assertThatThrownBy(() -> productUseCase.save(productoValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("precio no puede superar");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre supera 100 caracteres")
        void debeLanzarExcepcionNombreLargo() {
            productoValido.setName("A".repeat(101));
            assertThatThrownBy(() -> productUseCase.save(productoValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nombre no puede superar");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la descripción supera 500 caracteres")
        void debeLanzarExcepcionDescripcionLarga() {
            productoValido.setDescription("A".repeat(501));
            assertThatThrownBy(() -> productUseCase.save(productoValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("descripción no puede superar");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre está vacío")
        void debeLanzarExcepcionNombreVacio() {
            productoValido.setName("  ");
            assertThatThrownBy(() -> productUseCase.save(productoValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nombre no puede estar vacío");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la categoría está vacía")
        void debeLanzarExcepcionCategoriaVacia() {
            productoValido.setCategory("  ");
            assertThatThrownBy(() -> productUseCase.save(productoValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("categoría no puede estar vacía");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el tamaño está vacío")
        void debeLanzarExcepcionTamanioVacio() {
            productoValido.setSize("  ");
            assertThatThrownBy(() -> productUseCase.save(productoValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("tamaño es obligatorio");
        }
    }
}