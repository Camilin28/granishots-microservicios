package com.granishots.orders.infraestructure.entry_points;

import com.granishots.orders.application.dto.ApiResponse;
import com.granishots.orders.application.dto.OrderRequestDTO;
import com.granishots.orders.application.dto.OrderResponseDTO;
import com.granishots.orders.domain.usecase.OrderUseCase;
import com.granishots.orders.infraestructure.mapper.OrderMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCase orderUseCase;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDTO>> save(@Valid @RequestBody OrderRequestDTO dto) {
        return new ResponseEntity<>(
                ApiResponse.created("Pedido creado exitosamente",
                        orderMapper.toOrderResponseDTO(orderUseCase.save(orderMapper.toOrderFromDTO(dto)))),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> findAll() {
        List<OrderResponseDTO> list = orderUseCase.findAll().stream()
                .map(orderMapper::toOrderResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Pedidos obtenidos", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Pedido encontrado",
                orderMapper.toOrderResponseDTO(orderUseCase.findById(id))));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> findByStatus(@PathVariable String status) {
        List<OrderResponseDTO> list = orderUseCase.findByStatus(status).stream()
                .map(orderMapper::toOrderResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Pedidos por estado: " + status, list));
    }

    @GetMapping("/customer")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> findByCustomerPhone(@RequestParam String phone) {
        List<OrderResponseDTO> list = orderUseCase.findByCustomerPhone(phone).stream()
                .map(orderMapper::toOrderResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Pedidos del cliente", list));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> findToday() {
        List<OrderResponseDTO> list = orderUseCase.findToday().stream()
                .map(orderMapper::toOrderResponseDTO).toList();
        return ResponseEntity.ok(ApiResponse.ok("Pedidos de hoy", list));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null) throw new RuntimeException("El campo 'status' es obligatorio");
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado",
                orderMapper.toOrderResponseDTO(orderUseCase.updateStatus(id, status))));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Pedido cancelado",
                orderMapper.toOrderResponseDTO(orderUseCase.cancelOrder(id))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        orderUseCase.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Pedido " + id + " eliminado correctamente", null));
    }
}
