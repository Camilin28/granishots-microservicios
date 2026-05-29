package com.granishots.orders.domain.usecase;

import com.granishots.orders.domain.exception.BusinessException;
import com.granishots.orders.domain.model.Order;
import com.granishots.orders.domain.model.OrderItem;
import com.granishots.orders.domain.model.gateway.OrderGateway;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class OrderUseCase {

    private final OrderGateway orderGateway;

    private static final List<String> VALID_STATUSES = List.of(
            "PENDING", "CONFIRMED", "IN_PREPARATION", "READY", "DELIVERED", "CANCELLED");
    private static final List<String> VALID_CHANNELS = List.of("LOCAL", "DELIVERY", "PHONE");
    private static final int NOMBRE_MAX = 100;
    private static final int MAX_ITEMS = 50;
    private static final double PRECIO_MAX = 999_999;
    private static final int MAX_CANTIDAD = 100;

    public Order save(Order order) {
        validarOrder(order);
        order.setStatus("PENDING");
        double total = order.getItems().stream()
                .mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum();
        order.setTotal(Math.round(total * 100.0) / 100.0);
        return orderGateway.save(order);
    }

    public Order findById(Long id) {
        validarId(id);
        Order order = orderGateway.findById(id);
        if (order == null)
            throw new BusinessException("No existe un pedido con el id: " + id, 404);
        return order;
    }

    public List<Order> findAll() { return orderGateway.findAll(); }

    public List<Order> findByStatus(String status) {
        if (status == null || status.trim().isEmpty())
            throw new BusinessException("El estado no puede estar vacío", 400);
        String s = status.trim().toUpperCase();
        if (!VALID_STATUSES.contains(s))
            throw new BusinessException("Estado inválido: '" + status + "'. Válidos: " + VALID_STATUSES, 400);
        return orderGateway.findByStatus(s);
    }

    public List<Order> findByCustomerPhone(String phone) {
        if (phone == null || phone.trim().isEmpty())
            throw new BusinessException("El teléfono no puede estar vacío", 400);
        if (!phone.trim().matches("^[0-9]{7,10}$"))
            throw new BusinessException("El teléfono debe tener entre 7 y 10 dígitos. Recibido: '" + phone + "'", 400);
        return orderGateway.findByCustomerPhone(phone.trim());
    }

    public List<Order> findToday() { return orderGateway.findToday(); }

    public Order updateStatus(Long id, String status) {
        validarId(id);
        if (status == null || status.trim().isEmpty())
            throw new BusinessException("El estado no puede estar vacío", 400);
        String s = status.trim().toUpperCase();
        if (!VALID_STATUSES.contains(s))
            throw new BusinessException("Estado inválido: '" + status + "'. Válidos: " + VALID_STATUSES, 400);
        Order order = orderGateway.findById(id);
        if (order == null)
            throw new BusinessException("No existe un pedido con el id: " + id, 404);
        if ("DELIVERED".equals(order.getStatus()))
            throw new BusinessException("No se puede cambiar el estado de un pedido ya entregado", 400);
        if ("CANCELLED".equals(order.getStatus()))
            throw new BusinessException("No se puede cambiar el estado de un pedido cancelado", 400);
        if (order.getStatus().equals(s))
            throw new BusinessException("El pedido ya se encuentra en estado '" + s + "'", 400);
        return orderGateway.updateStatus(id, s);
    }

    public Order cancelOrder(Long id) {
        validarId(id);
        Order order = orderGateway.findById(id);
        if (order == null)
            throw new BusinessException("No existe un pedido con el id: " + id, 404);
        if ("DELIVERED".equals(order.getStatus()))
            throw new BusinessException("No se puede cancelar un pedido ya entregado", 400);
        if ("CANCELLED".equals(order.getStatus()))
            throw new BusinessException("El pedido ya está cancelado", 400);
        return orderGateway.updateStatus(id, "CANCELLED");
    }

    public void deleteById(Long id) {
        validarId(id);
        Order order = orderGateway.findById(id);
        if (order == null)
            throw new BusinessException("No existe un pedido con el id: " + id, 404);
        if (!"PENDING".equals(order.getStatus()))
            throw new BusinessException("Solo se pueden eliminar pedidos en estado PENDING. Estado actual: '" + order.getStatus() + "'", 400);
        orderGateway.deleteById(id);
    }

    private void validarId(Long id) {
        if (id == null) throw new BusinessException("El ID no puede ser nulo", 400);
        if (id <= 0) throw new BusinessException("El ID debe ser positivo. Recibido: " + id, 400);
    }

    private void validarOrder(Order order) {
        if (order == null) throw new BusinessException("El cuerpo del pedido no puede ser nulo", 400);
        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty())
            throw new BusinessException("El nombre del cliente no puede estar vacío", 400);
        if (order.getCustomerName().trim().length() > NOMBRE_MAX)
            throw new BusinessException("El nombre del cliente no puede superar " + NOMBRE_MAX + " caracteres", 400);
        if (order.getCustomerPhone() != null && !order.getCustomerPhone().trim().isEmpty()) {
            if (!order.getCustomerPhone().trim().matches("^[0-9]{7,10}$"))
                throw new BusinessException("El teléfono debe tener entre 7 y 10 dígitos. Recibido: '" + order.getCustomerPhone() + "'", 400);
        }
        if (order.getChannel() == null || order.getChannel().trim().isEmpty())
            throw new BusinessException("El canal del pedido no puede estar vacío", 400);
        String channel = order.getChannel().trim().toUpperCase();
        if (!VALID_CHANNELS.contains(channel))
            throw new BusinessException("Canal inválido: '" + order.getChannel() + "'. Válidos: " + VALID_CHANNELS, 400);
        order.setChannel(channel);
        if (order.getTableNumber() != null && (order.getTableNumber() < 1 || order.getTableNumber() > 200))
            throw new BusinessException("El número de mesa debe estar entre 1 y 200. Recibido: " + order.getTableNumber(), 400);
        if (order.getItems() == null || order.getItems().isEmpty())
            throw new BusinessException("El pedido debe tener al menos un ítem", 400);
        if (order.getItems().size() > MAX_ITEMS)
            throw new BusinessException("El pedido no puede tener más de " + MAX_ITEMS + " ítems. Recibido: " + order.getItems().size(), 400);
        for (int i = 0; i < order.getItems().size(); i++) {
            validarItem(order.getItems().get(i), i + 1);
        }
    }

    private void validarItem(OrderItem item, int pos) {
        String prefix = "Ítem #" + pos + ": ";
        if (item.getProductId() == null)
            throw new BusinessException(prefix + "el ID del producto es obligatorio", 400);
        if (item.getProductId() <= 0)
            throw new BusinessException(prefix + "el ID del producto debe ser positivo", 400);
        if (item.getProductName() == null || item.getProductName().trim().isEmpty())
            throw new BusinessException(prefix + "el nombre del producto es obligatorio", 400);
        if (item.getQuantity() == null)
            throw new BusinessException(prefix + "la cantidad es obligatoria", 400);
        if (item.getQuantity() < 1)
            throw new BusinessException(prefix + "la cantidad mínima es 1. Recibido: " + item.getQuantity(), 400);
        if (item.getQuantity() > MAX_CANTIDAD)
            throw new BusinessException(prefix + "la cantidad no puede superar " + MAX_CANTIDAD, 400);
        if (item.getUnitPrice() == null)
            throw new BusinessException(prefix + "el precio unitario es obligatorio", 400);
        if (item.getUnitPrice() <= 0)
            throw new BusinessException(prefix + "el precio debe ser mayor a cero. Recibido: " + item.getUnitPrice(), 400);
        if (item.getUnitPrice() > PRECIO_MAX)
            throw new BusinessException(prefix + "el precio no puede superar " + PRECIO_MAX, 400);
    }
}
