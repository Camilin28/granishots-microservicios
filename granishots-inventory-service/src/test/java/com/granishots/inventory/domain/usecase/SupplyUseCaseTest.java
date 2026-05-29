package com.granishots.inventory.domain.usecase;

import com.granishots.inventory.domain.exception.BusinessException;
import com.granishots.inventory.domain.model.Supply;
import com.granishots.inventory.domain.model.gateway.SupplyGateway;
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
@DisplayName("SupplyUseCase — Pruebas unitarias")
class SupplyUseCaseTest {

    @Mock
    private SupplyGateway supplyGateway;

    @InjectMocks
    private SupplyUseCase supplyUseCase;

    private Supply supplyValido;

    @BeforeEach
    void setUp() {
        supplyValido = new Supply(null, "Café molido", "KG", 50.0, 10.0, "Proveedor X", 15000.0);
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("Debe guardar un insumo válido y retornarlo")
        void debeGuardarInsumoValido() {
            Supply guardado = new Supply(1L, "Café molido", "KG", 50.0, 10.0, "Proveedor X", 15000.0);
            when(supplyGateway.findByName("Café molido")).thenReturn(List.of());
            when(supplyGateway.save(any())).thenReturn(guardado);

            Supply resultado = supplyUseCase.save(supplyValido);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getName()).isEqualTo("Café molido");
            verify(supplyGateway).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre ya existe")
        void debeLanzarExcepcionNombreDuplicado() {
            Supply existente = new Supply(1L, "Café molido", "KG", 20.0, 5.0, null, null);
            when(supplyGateway.findByName("Café molido")).thenReturn(List.of(existente));

            assertThatThrownBy(() -> supplyUseCase.save(supplyValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Ya existe un insumo con el nombre");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre está vacío")
        void debeLanzarExcepcionNombreVacio() {
            supplyValido.setName("  ");
            assertThatThrownBy(() -> supplyUseCase.save(supplyValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nombre del insumo no puede estar vacío");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la unidad es inválida")
        void debeLanzarExcepcionUnidadInvalida() {
            supplyValido.setUnit("LIBRA");
            assertThatThrownBy(() -> supplyUseCase.save(supplyValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Unidad inválida");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el stock actual es negativo")
        void debeLanzarExcepcionStockNegativo() {
            supplyValido.setCurrentStock(-1.0);
            assertThatThrownBy(() -> supplyUseCase.save(supplyValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("stock actual no puede ser negativo");
        }

        @Test
        @DisplayName("Debe lanzar excepción si minStock > currentStock")
        void debeLanzarExcepcionMinStockMayorQueCurrentStock() {
            supplyValido.setMinStock(100.0);
            supplyValido.setCurrentStock(10.0);
            assertThatThrownBy(() -> supplyUseCase.save(supplyValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("stock mínimo");
        }

        @Test
        @DisplayName("Debe normalizar nombre y unidad (trim + toUpperCase)")
        void debeNormalizarNombreYUnidad() {
            supplyValido.setName("  azúcar  ");
            supplyValido.setUnit("  kg  ");
            Supply guardado = new Supply(1L, "azúcar", "KG", 50.0, 10.0, null, null);
            when(supplyGateway.findByName("azúcar")).thenReturn(List.of());
            when(supplyGateway.save(any())).thenReturn(guardado);

            supplyUseCase.save(supplyValido);

            verify(supplyGateway).findByName("azúcar");
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Debe retornar el insumo cuando existe")
        void debeRetornarInsumo() {
            Supply supply = new Supply(1L, "Leche", "L", 20.0, 5.0, null, null);
            when(supplyGateway.findById(1L)).thenReturn(supply);

            Supply resultado = supplyUseCase.findById(1L);

            assertThat(resultado.getName()).isEqualTo("Leche");
        }

        @Test
        @DisplayName("Debe lanzar excepción 404 si no existe")
        void debeLanzarExcepcion404() {
            when(supplyGateway.findById(99L)).thenReturn(null);

            assertThatThrownBy(() -> supplyUseCase.findById(99L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un insumo con el id: 99");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el ID es negativo")
        void debeLanzarExcepcionIdNegativo() {
            assertThatThrownBy(() -> supplyUseCase.findById(-1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ID debe ser un número positivo");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el ID es nulo")
        void debeLanzarExcepcionIdNulo() {
            assertThatThrownBy(() -> supplyUseCase.findById(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ID no puede ser nulo");
        }
    }

    @Nested
    @DisplayName("findByName()")
    class FindByName {

        @Test
        @DisplayName("Debe retornar resultados cuando el nombre es válido")
        void debeRetornarResultados() {
            when(supplyGateway.findByName("café")).thenReturn(List.of(supplyValido));

            List<Supply> resultado = supplyUseCase.findByName("café");

            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Debe lanzar excepción si el término está vacío")
        void debeLanzarExcepcionTerminoVacio() {
            assertThatThrownBy(() -> supplyUseCase.findByName("  "))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("término de búsqueda no puede estar vacío");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el término tiene menos de 2 caracteres")
        void debeLanzarExcepcionTerminoCorto() {
            assertThatThrownBy(() -> supplyUseCase.findByName("a"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("al menos 2 caracteres");
        }
    }

    @Nested
    @DisplayName("registerEntry()")
    class RegisterEntry {

        @Test
        @DisplayName("Debe registrar una entrada válida")
        void debeRegistrarEntrada() {
            Supply supply = new Supply(1L, "Café", "KG", 50.0, 5.0, null, null);
            Supply actualizado = new Supply(1L, "Café", "KG", 60.0, 5.0, null, null);
            when(supplyGateway.findById(1L)).thenReturn(supply);
            when(supplyGateway.addStock(1L, 10.0)).thenReturn(actualizado);

            Supply resultado = supplyUseCase.registerEntry(1L, 10.0);

            assertThat(resultado.getCurrentStock()).isEqualTo(60.0);
        }

        @Test
        @DisplayName("Debe lanzar excepción si la cantidad es cero")
        void debeLanzarExcepcionCantidadCero() {
            assertThatThrownBy(() -> supplyUseCase.registerEntry(1L, 0.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("mayor a cero");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la entrada supera el stock máximo")
        void debeLanzarExcepcionStockMaximo() {
            Supply supply = new Supply(1L, "Café", "KG", 999_990.0, 5.0, null, null);
            when(supplyGateway.findById(1L)).thenReturn(supply);

            assertThatThrownBy(() -> supplyUseCase.registerEntry(1L, 100.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("supera el máximo permitido");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el insumo no existe")
        void debeLanzarExcepcionInsumoNoExiste() {
            when(supplyGateway.findById(99L)).thenReturn(null);

            assertThatThrownBy(() -> supplyUseCase.registerEntry(99L, 10.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un insumo con el id: 99");
        }
    }

    @Nested
    @DisplayName("registerExit()")
    class RegisterExit {

        @Test
        @DisplayName("Debe registrar una salida válida")
        void debeRegistrarSalida() {
            Supply supply = new Supply(1L, "Café", "KG", 50.0, 5.0, null, null);
            Supply actualizado = new Supply(1L, "Café", "KG", 40.0, 5.0, null, null);
            when(supplyGateway.findById(1L)).thenReturn(supply);
            when(supplyGateway.subtractStock(1L, 10.0)).thenReturn(actualizado);

            Supply resultado = supplyUseCase.registerExit(1L, 10.0);

            assertThat(resultado.getCurrentStock()).isEqualTo(40.0);
        }

        @Test
        @DisplayName("Debe lanzar excepción si el stock es insuficiente")
        void debeLanzarExcepcionStockInsuficiente() {
            Supply supply = new Supply(1L, "Café", "KG", 5.0, 2.0, null, null);
            when(supplyGateway.findById(1L)).thenReturn(supply);

            assertThatThrownBy(() -> supplyUseCase.registerExit(1L, 20.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Stock insuficiente");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el insumo no tiene stock")
        void debeLanzarExcepcionSinStock() {
            Supply supply = new Supply(1L, "Café", "KG", 0.0, 0.0, null, null);
            when(supplyGateway.findById(1L)).thenReturn(supply);

            assertThatThrownBy(() -> supplyUseCase.registerExit(1L, 5.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no tiene stock disponible");
        }
    }

    @Nested
    @DisplayName("deleteById()")
    class DeleteById {

        @Test
        @DisplayName("Debe eliminar el insumo cuando existe")
        void debeEliminarInsumo() {
            when(supplyGateway.findById(1L)).thenReturn(supplyValido);
            doNothing().when(supplyGateway).deleteById(1L);

            supplyUseCase.deleteById(1L);

            verify(supplyGateway).deleteById(1L);
        }

        @Test
        @DisplayName("Debe lanzar excepción si el insumo no existe")
        void debeLanzarExcepcionNoExiste() {
            when(supplyGateway.findById(5L)).thenReturn(null);

            assertThatThrownBy(() -> supplyUseCase.deleteById(5L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un insumo con el id: 5");
        }
    }

    @Nested
    @DisplayName("updateMinStock()")
    class UpdateMinStock {

        @Test
        @DisplayName("Debe actualizar el stock mínimo correctamente")
        void debeActualizarMinStock() {
            Supply supply = new Supply(1L, "Café", "KG", 50.0, 10.0, null, null);
            Supply actualizado = new Supply(1L, "Café", "KG", 50.0, 15.0, null, null);
            when(supplyGateway.findById(1L)).thenReturn(supply);
            when(supplyGateway.updateMinStock(1L, 15.0)).thenReturn(actualizado);

            Supply resultado = supplyUseCase.updateMinStock(1L, 15.0);

            assertThat(resultado.getMinStock()).isEqualTo(15.0);
        }

        @Test
        @DisplayName("Debe lanzar excepción si minStock > currentStock")
        void debeLanzarExcepcionMinMayorQueActual() {
            Supply supply = new Supply(1L, "Café", "KG", 10.0, 5.0, null, null);
            when(supplyGateway.findById(1L)).thenReturn(supply);

            assertThatThrownBy(() -> supplyUseCase.updateMinStock(1L, 99.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no puede ser mayor al stock actual");
        }

        @Test
        @DisplayName("Debe lanzar excepción si minStock es negativo")
        void debeLanzarExcepcionNegativo() {
            assertThatThrownBy(() -> supplyUseCase.updateMinStock(1L, -5.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no puede ser negativo");
        }

        @Test
        @DisplayName("Debe lanzar excepción si minStock es nulo")
        void debeLanzarExcepcionNulo() {
            assertThatThrownBy(() -> supplyUseCase.updateMinStock(1L, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("stock mínimo no puede ser nulo");
        }

        @Test
        @DisplayName("Debe lanzar excepción si minStock supera el máximo permitido")
        void debeLanzarExcepcionSuperaMaximo() {
            assertThatThrownBy(() -> supplyUseCase.updateMinStock(1L, 1_000_000.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no puede superar");
        }
    }

    @Nested
    @DisplayName("findAll() / findLowStock() / findOutOfStock()")
    class Consultas {

        @Test
        @DisplayName("findAll debe retornar todos los insumos")
        void findAllDebeRetornarTodos() {
            when(supplyGateway.findAll()).thenReturn(List.of(supplyValido, supplyValido));

            List<Supply> resultado = supplyUseCase.findAll();

            assertThat(resultado).hasSize(2);
            verify(supplyGateway).findAll();
        }

        @Test
        @DisplayName("findLowStock debe retornar insumos con bajo stock")
        void findLowStockDebeRetornarLista() {
            when(supplyGateway.findLowStock()).thenReturn(List.of(supplyValido));

            List<Supply> resultado = supplyUseCase.findLowStock();

            assertThat(resultado).hasSize(1);
            verify(supplyGateway).findLowStock();
        }

        @Test
        @DisplayName("findOutOfStock debe retornar insumos sin stock")
        void findOutOfStockDebeRetornarLista() {
            when(supplyGateway.findOutOfStock()).thenReturn(List.of());

            List<Supply> resultado = supplyUseCase.findOutOfStock();

            assertThat(resultado).isEmpty();
            verify(supplyGateway).findOutOfStock();
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Debe actualizar un insumo existente correctamente")
        void debeActualizarInsumo() {
            Supply existente = new Supply(1L, "Café molido", "KG", 50.0, 10.0, null, null);
            Supply actualizado = new Supply(1L, "Café premium", "KG", 50.0, 10.0, null, null);
            when(supplyGateway.findById(1L)).thenReturn(existente);
            when(supplyGateway.findByName("Café premium")).thenReturn(List.of());
            when(supplyGateway.update(any())).thenReturn(actualizado);

            Supply nuevosDatos = new Supply(null, "Café premium", "KG", 50.0, 10.0, null, null);
            Supply resultado = supplyUseCase.update(1L, nuevosDatos);

            assertThat(resultado.getName()).isEqualTo("Café premium");
            verify(supplyGateway).update(any());
        }

        @Test
        @DisplayName("Debe lanzar 404 si el insumo a actualizar no existe")
        void debeLanzar404() {
            when(supplyGateway.findById(99L)).thenReturn(null);

            Supply datos = new Supply(null, "Café", "KG", 50.0, 10.0, null, null);
            assertThatThrownBy(() -> supplyUseCase.update(99L, datos))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un insumo con el id: 99");
        }

        @Test
        @DisplayName("Debe lanzar 409 si el nuevo nombre ya existe en otro insumo")
        void debeLanzar409NombreDuplicado() {
            Supply existente = new Supply(1L, "Café molido", "KG", 50.0, 10.0, null, null);
            Supply otroConMismoNombre = new Supply(2L, "Café premium", "KG", 30.0, 5.0, null, null);
            when(supplyGateway.findById(1L)).thenReturn(existente);
            when(supplyGateway.findByName("Café premium")).thenReturn(List.of(otroConMismoNombre));

            Supply datos = new Supply(null, "Café premium", "KG", 50.0, 10.0, null, null);
            assertThatThrownBy(() -> supplyUseCase.update(1L, datos))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Ya existe otro insumo con el nombre");
        }

        @Test
        @DisplayName("Debe permitir actualizar manteniendo el mismo nombre")
        void debePermitirMismoNombre() {
            Supply existente = new Supply(1L, "Café molido", "KG", 50.0, 10.0, null, null);
            // findByName retorna el mismo insumo (mismo ID), no es duplicado
            when(supplyGateway.findById(1L)).thenReturn(existente);
            when(supplyGateway.findByName("Café molido")).thenReturn(List.of(existente));
            when(supplyGateway.update(any())).thenReturn(existente);

            Supply datos = new Supply(null, "Café molido", "KG", 60.0, 10.0, null, null);
            assertThatCode(() -> supplyUseCase.update(1L, datos)).doesNotThrowAnyException();
        }
    }
    @Nested
    @DisplayName("validarSupply() — casos límite")
    class ValidarSupplyCasosLimite {

        @Test
        @DisplayName("Debe lanzar excepción si el stock actual supera el máximo")
        void debeLanzarExcepcionStockSuperaMaximo() {
            supplyValido.setCurrentStock(1_000_000.0);
            supplyValido.setMinStock(0.0);
            assertThatThrownBy(() -> supplyUseCase.save(supplyValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("stock no puede superar");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el stock mínimo es nulo")
        void debeLanzarExcepcionMinStockNulo() {
            supplyValido.setMinStock(null);
            assertThatThrownBy(() -> supplyUseCase.save(supplyValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("stock mínimo es obligatorio");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el precio de compra es negativo")
        void debeLanzarExcepcionPrecioNegativo() {
            supplyValido.setPurchasePrice(-100.0);
            assertThatThrownBy(() -> supplyUseCase.save(supplyValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("precio de compra no puede ser negativo");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre supera 100 caracteres")
        void debeLanzarExcepcionNombreLargo() {
            supplyValido.setName("A".repeat(101));
            assertThatThrownBy(() -> supplyUseCase.save(supplyValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nombre no puede superar");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el stock actual es nulo")
        void debeLanzarExcepcionStockNulo() {
            supplyValido.setCurrentStock(null);
            assertThatThrownBy(() -> supplyUseCase.save(supplyValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("stock actual es obligatorio");
        }
    }
}