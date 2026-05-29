package com.granishots.orders.domain.usecase;

import com.granishots.orders.domain.exception.BusinessException;
import com.granishots.orders.domain.model.Order;
import com.granishots.orders.domain.model.OrderItem;
import com.granishots.orders.domain.model.gateway.OrderGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderUseCase — Pruebas unitarias")
class OrderUseCaseTest {

    @Mock
    private OrderGateway orderGateway;

    @InjectMocks
    private OrderUseCase orderUseCase;

    private Order orderValido;
    private OrderItem itemValido;

    @BeforeEach
    void setUp() {

        itemValido = new OrderItem(null, null, 1L, "Granizado de fresa", 2, 8000.0, "8", null, null, null);
        orderValido = new Order(null, "Juan Pérez", "3001234567", null, "LOCAL", 5, null, null, null, new ArrayList<>(List.of(itemValido)));
    }
    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("Debe guardar un pedido válido con estado PENDING y calcular el total")
        void debeGuardarPedidoValido() {
            Order guardado = new Order(1L, "Juan Pérez", "3001234567", "PENDING", "LOCAL", 5, null, 16000.0, null, List.of(itemValido)
            );
            when(orderGateway.save(any())).thenReturn(guardado);
            Order resultado = orderUseCase.save(orderValido);
            assertThat(resultado.getStatus()).isEqualTo("PENDING");
            assertThat(resultado.getTotal()).isEqualTo(16000.0);
            verify(orderGateway).save(any());
        }

        @Test
        @DisplayName("Debe calcular el total correctamente como suma de items")
        void debeCalcularTotalCorrectamente() {
            OrderItem item2 = new OrderItem(null, null, 2L, "Otro", 1, 5000.0, "8", null, null, null);
            orderValido.setItems(new ArrayList<>(List.of(itemValido, item2)));
            Order guardado = new Order(1L, "Juan", null, "PENDING", "LOCAL", null, null, 21000.0, null, orderValido.getItems());
            when(orderGateway.save(any())).thenReturn(guardado);
            Order resultado = orderUseCase.save(orderValido);
            assertThat(resultado.getTotal()).isEqualTo(21000.0);
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre del cliente está vacío")
        void debeLanzarExcepcionClienteVacio() {
            orderValido.setCustomerName(" ");
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nombre del cliente no puede estar vacío");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el canal es inválido")
        void debeLanzarExcepcionCanalInvalido() {
            orderValido.setChannel("DRIVE_THRU");
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Canal inválido");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el canal está vacío")
        void debeLanzarExcepcionCanalVacio() {
            orderValido.setChannel(" ");
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("canal del pedido no puede estar vacío");
        }
        @Test
        @DisplayName("Debe lanzar excepción si la lista de ítems está vacía")
        void debeLanzarExcepcionSinItems() {
            orderValido.setItems(List.of());
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("al menos un ítem");
        }
        @Test
        @DisplayName("Debe lanzar excepción si un ítem tiene precio cero")
        void debeLanzarExcepcionItemPrecioCero() {
            itemValido.setUnitPrice(0.0);
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("precio debe ser mayor a cero");
        }
        @Test
        @DisplayName("Debe lanzar excepción si un ítem tiene precio nulo")
        void debeLanzarExcepcionPrecioNulo() {
            itemValido.setUnitPrice(null);
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("precio unitario es obligatorio");
        }
        @Test
        @DisplayName("Debe lanzar excepción si un ítem tiene cantidad cero")
        void debeLanzarExcepcionItemCantidadCero() {
            itemValido.setQuantity(0);
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cantidad mínima es 1");
        }
        @Test
        @DisplayName("Debe lanzar excepción si la cantidad es nula")
        void debeLanzarExcepcionCantidadNula() {
            itemValido.setQuantity(null);
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cantidad es obligatoria");
        }
        @Test
        @DisplayName("Debe lanzar excepción si la cantidad supera 100")
        void debeLanzarExcepcionCantidadMayorMaxima() {
            itemValido.setQuantity(101);
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cantidad no puede superar");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el teléfono tiene menos de 7 dígitos")
        void debeLanzarExcepcionTelefonoCorto() {
            orderValido.setCustomerPhone("123456");
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("entre 7 y 10 dígitos");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el número de mesa es inválido")
        void debeLanzarExcepcionMesaInvalida() {
            orderValido.setTableNumber(0);
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("número de mesa debe estar entre 1 y 200");
        }
        @Test
        @DisplayName("Debe aceptar los tres canales válidos")
        void debeAceptarCanalesValidos() {
            for (String canal : List.of("LOCAL", "DELIVERY", "PHONE")) {
                orderValido.setChannel(canal);
                when(orderGateway.save(any())).thenReturn(orderValido);
                assertThatCode(() -> orderUseCase.save(orderValido))
                        .doesNotThrowAnyException();
            }
        }
        @Test
        @DisplayName("Debe lanzar excepción si supera el máximo de items")
        void debeLanzarExcepcionMaximoItems() {
            List<OrderItem> items = new ArrayList<>();
            for (int i = 0; i < 51; i++) {

                items.add(
                        new OrderItem(null, null, 1L, "Producto", 1, 1000.0, "8", null, null, null)
                );
            }
            orderValido.setItems(items);
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("más de 50 ítems");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el precio supera el máximo")
        void debeLanzarExcepcionPrecioMayorMaximo() {
            itemValido.setUnitPrice(1000000.0);
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("precio no puede superar");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el productId es nulo")
        void debeLanzarExcepcionProductIdNulo() {
            itemValido.setProductId(null);
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ID del producto es obligatorio");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el productId es inválido")
        void debeLanzarExcepcionProductIdInvalido() {
            itemValido.setProductId(0L);
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ID del producto debe ser positivo");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el nombre del producto está vacío")
        void debeLanzarExcepcionNombreProductoVacio() {
            itemValido.setProductName(" ");
            assertThatThrownBy(() -> orderUseCase.save(orderValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nombre del producto es obligatorio");
        }
    }
    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {
        @Test
        @DisplayName("Debe actualizar el estado correctamente")
        void debeActualizarEstado() {
            Order order = new Order(1L, "Juan", null, "PENDING", "LOCAL", null, null, 16000.0, null, List.of());
            Order actualizado = new Order(1L, "Juan", null, "CONFIRMED", "LOCAL", null, null, 16000.0, null, List.of());
            when(orderGateway.findById(1L)).thenReturn(order);
            when(orderGateway.updateStatus(1L, "CONFIRMED"))
                    .thenReturn(actualizado);
            Order resultado =
                    orderUseCase.updateStatus(1L, "CONFIRMED");
            assertThat(resultado.getStatus())
                    .isEqualTo("CONFIRMED");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el pedido ya fue entregado")
        void debeLanzarExcepcionEntregado() {
            Order order = new Order(1L, "Juan", null, "DELIVERED", "LOCAL", null, null, 16000.0, null, List.of());
            when(orderGateway.findById(1L)).thenReturn(order);
            assertThatThrownBy(() ->
                    orderUseCase.updateStatus(1L, "CONFIRMED"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ya entregado");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el pedido ya fue cancelado")
        void debeLanzarExcepcionCancelado() {
            Order order = new Order(1L, "Juan", null, "CANCELLED", "LOCAL", null, null, 16000.0, null, List.of());
            when(orderGateway.findById(1L)).thenReturn(order);
            assertThatThrownBy(() ->
                    orderUseCase.updateStatus(1L, "CONFIRMED"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cancelado");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el estado ya es el mismo")
        void debeLanzarExcepcionMismoEstado() {
            Order order = new Order(1L, "Juan", null, "PENDING", "LOCAL", null, null, 16000.0, null, List.of());
            when(orderGateway.findById(1L)).thenReturn(order);
            assertThatThrownBy(() -> orderUseCase.updateStatus(1L, "PENDING"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ya se encuentra en estado");
        }
        @Test
        @DisplayName("Debe lanzar excepción con estado inválido")
        void debeLanzarExcepcionEstadoInvalido() {
            assertThatThrownBy(() -> orderUseCase.updateStatus(1L, "PAGADO")).isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Estado inválido");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el pedido no existe al actualizar estado")
        void debeLanzarExcepcionUpdatePedidoNoExiste() {

            when(orderGateway.findById(1L)).thenReturn(null);

            assertThatThrownBy(() ->
                    orderUseCase.updateStatus(1L, "CONFIRMED"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un pedido");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el estado está vacío")
        void debeLanzarExcepcionEstadoVacioUpdate() {

            assertThatThrownBy(() ->
                    orderUseCase.updateStatus(1L, " "))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("estado no puede estar vacío");
        }
    }
    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrder {
        @Test
        @DisplayName("Debe cancelar un pedido en estado PENDING")
        void debeCancelarPedido() {
            Order order = new Order(1L, "Juan", null, "PENDING", "LOCAL", null, null, 16000.0, null, List.of());
            Order cancelado = new Order(1L, "Juan", null, "CANCELLED", "LOCAL", null, null, 16000.0, null, List.of());
            when(orderGateway.findById(1L)).thenReturn(order);
            when(orderGateway.updateStatus(1L, "CANCELLED")).thenReturn(cancelado);
            Order resultado = orderUseCase.cancelOrder(1L);
            assertThat(resultado.getStatus())
                    .isEqualTo("CANCELLED");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el pedido ya está cancelado")
        void debeLanzarExcepcionYaCancelado() {
            Order order = new Order(1L, "Juan", null, "CANCELLED", "LOCAL", null, null, 16000.0, null, List.of());
            when(orderGateway.findById(1L)).thenReturn(order);
            assertThatThrownBy(() -> orderUseCase.cancelOrder(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ya está cancelado");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el pedido ya fue entregado")
        void debeLanzarExcepcionYaEntregado() {
            Order order = new Order(1L, "Juan", null, "DELIVERED", "LOCAL", null, null, 16000.0, null, List.of());
            when(orderGateway.findById(1L)).thenReturn(order);
            assertThatThrownBy(() -> orderUseCase.cancelOrder(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ya entregado");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el pedido no existe al cancelar")
        void debeLanzarExcepcionCancelPedidoNoExiste() {

            when(orderGateway.findById(1L)).thenReturn(null);

            assertThatThrownBy(() -> orderUseCase.cancelOrder(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un pedido");
        }
    }
    @Nested
    @DisplayName("deleteById()")
    class DeleteById {

        @Test
        @DisplayName("Debe eliminar un pedido en estado PENDING")
        void debeEliminarPedidoPending() {

            Order order = new Order(1L, "Juan", null, "PENDING", "LOCAL", null, null, 16000.0, null, List.of());
            when(orderGateway.findById(1L)).thenReturn(order);
            doNothing().when(orderGateway).deleteById(1L);
            orderUseCase.deleteById(1L);
            verify(orderGateway).deleteById(1L);
        }

        @Test
        @DisplayName("Debe lanzar excepción si el pedido no está en PENDING")
        void debeLanzarExcepcionNoPending() {

            Order order = new Order(1L, "Juan", null, "CONFIRMED", "LOCAL", null, null, 16000.0, null, List.of());
            when(orderGateway.findById(1L)).thenReturn(order);
            assertThatThrownBy(() -> orderUseCase.deleteById(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(
                            "Solo se pueden eliminar pedidos en estado PENDING"
                    );
        }
        @Test
        @DisplayName("Debe lanzar excepción si el pedido no existe al eliminar")
        void debeLanzarExcepcionDeletePedidoNoExiste() {

            when(orderGateway.findById(1L)).thenReturn(null);

            assertThatThrownBy(() -> orderUseCase.deleteById(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No existe un pedido");
        }
    }
    @Test
    @DisplayName("Debe retornar un pedido por ID")
    void debeRetornarPedidoPorId() {

        Order order = new Order(
                1L, "Juan", null, "PENDING", "LOCAL", null, null, 16000.0, null, List.of());
        when(orderGateway.findById(1L)).thenReturn(order);
        Order resultado = orderUseCase.findById(1L);
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(orderGateway).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el pedido no existe")
    void debeLanzarExcepcionPedidoNoExiste() {
        when(orderGateway.findById(1L)).thenReturn(null);
        assertThatThrownBy(() -> orderUseCase.findById(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No existe un pedido");
    }
    @Test
    @DisplayName("Debe lanzar excepción si el ID es nulo")
    void debeLanzarExcepcionIdNulo() {
        assertThatThrownBy(() -> orderUseCase.findById(null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ID no puede ser nulo");
    }
    @Test
    @DisplayName("Debe lanzar excepción si el ID es negativo")
    void debeLanzarExcepcionIdNegativo() {
        assertThatThrownBy(() -> orderUseCase.findById(-1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ID debe ser positivo");
    }
    @Test
    @DisplayName("Debe retornar pedidos por estado válido")
    void debeRetornarPedidosPorEstado() {
        when(orderGateway.findByStatus("PENDING"))
                .thenReturn(List.of(orderValido));
        List<Order> resultado = orderUseCase.findByStatus("PENDING");
        assertThat(resultado).hasSize(1);
        verify(orderGateway).findByStatus("PENDING");
    }
    @Test
    @DisplayName("Debe lanzar excepción si el nombre supera el máximo permitido")
    void debeLanzarExcepcionNombreMuyLargo() {

        String nombreLargo = "a".repeat(101);

        orderValido.setCustomerName(nombreLargo);

        assertThatThrownBy(() -> orderUseCase.save(orderValido))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no puede superar");
    }
    @Test
    @DisplayName("Debe lanzar excepción si el canal está vacío")
    void debeLanzarExcepcionCanalVacio() {

        orderValido.setChannel(" ");

        assertThatThrownBy(() -> orderUseCase.save(orderValido))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("canal del pedido no puede estar vacío");
    }
    @Nested
    @DisplayName("findByCustomerPhone()")
    class FindByPhone {
        @Test
        @DisplayName("Debe retornar pedidos por teléfono válido")
        void debeRetornarPedidos() {
            when(orderGateway.findByCustomerPhone("3001234567"))
                    .thenReturn(List.of(orderValido));
            List<Order> resultado = orderUseCase.findByCustomerPhone("3001234567");
            assertThat(resultado).hasSize(1);
        }
        @Test
        @DisplayName("Debe lanzar excepción si el teléfono tiene letras")
        void debeLanzarExcepcionTelefonoConLetras() {
            assertThatThrownBy(() -> orderUseCase.findByCustomerPhone("abc123"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("entre 7 y 10 dígitos");
        }
        @Test
        @DisplayName("Debe lanzar excepción si el teléfono está vacío")
        void debeLanzarExcepcionTelefonoVacio() {

            assertThatThrownBy(() ->
                    orderUseCase.findByCustomerPhone(" "))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("teléfono no puede estar vacío");
        }
    }
    @Nested
    @DisplayName("findByStatus()")
    class FindByStatus {
        @Test
        @DisplayName("Debe lanzar excepción si el estado es inválido")
        void debeLanzarExcepcionEstadoInvalidoFindByStatus() {

            assertThatThrownBy(() ->
                    orderUseCase.findByStatus("PAGADO"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Estado inválido");
        }

    }
    @Test
    @DisplayName("Debe retornar todos los pedidos")
    void debeRetornarTodosLosPedidos() {

        when(orderGateway.findAll())
                .thenReturn(List.of(orderValido));

        List<Order> resultado = orderUseCase.findAll();

        assertThat(resultado).hasSize(1);

        verify(orderGateway).findAll();
    }
    @Test
    @DisplayName("Debe lanzar excepción si el estado está vacío en findByStatus")
    void debeLanzarExcepcionEstadoVacioFindByStatus() {

        assertThatThrownBy(() ->
                orderUseCase.findByStatus(" "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("estado no puede estar vacío");
    }
    @Test
    @DisplayName("Debe lanzar excepción si el estado es inválido")
    void debeLanzarExcepcionEstadoInvalidoFindByStatus() {

        assertThatThrownBy(() ->
                orderUseCase.findByStatus("PAGADO"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estado inválido");
    }
    @Test
    @DisplayName("Debe retornar los pedidos del día")
    void debeRetornarPedidosDelDia() {
        when(orderGateway.findToday())
                .thenReturn(List.of(orderValido));
        List<Order> resultado = orderUseCase.findToday();
        assertThat(resultado).hasSize(1);
        verify(orderGateway).findToday();
    }

}