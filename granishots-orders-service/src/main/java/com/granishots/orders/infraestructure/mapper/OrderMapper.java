package com.granishots.orders.infraestructure.mapper;

import com.granishots.orders.application.dto.*;
import com.granishots.orders.domain.model.Order;
import com.granishots.orders.domain.model.OrderItem;
import com.granishots.orders.infraestructure.driver_adapters.jpa_repository.OrderData;
import com.granishots.orders.infraestructure.driver_adapters.jpa_repository.OrderItemData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public Order toOrder(OrderData data) {
        List<OrderItem> items = data.getItems() == null ? List.of() :
                data.getItems().stream().map(this::toOrderItem).toList();
        return new Order(
                data.getId(), data.getCustomerName(), data.getCustomerPhone(),
                data.getStatus(), data.getChannel(), data.getTableNumber(),
                data.getNotes(), data.getTotal(), data.getCreatedAt(), items
        );
    }

    public OrderItem toOrderItem(OrderItemData data) {
        return new OrderItem(data.getId(), data.getOrder() != null ? data.getOrder().getId() : null,
                data.getProductId(), data.getProductName(), data.getQuantity(),
                data.getUnitPrice(), data.getSize(), data.getFlavor(), data.getToppings(), data.getNotes());
    }

    public OrderData toOrderData(Order order) {
        OrderData data = new OrderData(
                order.getId(), order.getCustomerName(), order.getCustomerPhone(),
                order.getStatus(), order.getChannel(), order.getTableNumber(),
                order.getNotes(), order.getTotal(), order.getCreatedAt(), null
        );
        if (order.getItems() != null) {
            List<OrderItemData> items = order.getItems().stream().map(i -> {
                OrderItemData itemData = new OrderItemData();
                itemData.setOrder(data);
                itemData.setProductId(i.getProductId());
                itemData.setProductName(i.getProductName());
                itemData.setQuantity(i.getQuantity());
                itemData.setUnitPrice(i.getUnitPrice());
                itemData.setSize(i.getSize());
                itemData.setFlavor(i.getFlavor());
                itemData.setToppings(i.getToppings());
                itemData.setNotes(i.getNotes());
                return itemData;
            }).toList();
            data.setItems(items);
        }
        return data;
    }

    public Order toOrderFromDTO(OrderRequestDTO dto) {
        List<OrderItem> items = dto.getItems().stream().map(i ->
                new OrderItem(null, null, i.getProductId(), i.getProductName(),
                        i.getQuantity(), i.getUnitPrice(), i.getSize(),
                        i.getFlavor(), i.getToppings(), i.getNotes())
        ).toList();
        return new Order(null, dto.getCustomerName(), dto.getCustomerPhone(),
                "PENDING", dto.getChannel(), dto.getTableNumber(),
                dto.getNotes(), 0.0, null, items);
    }

    public OrderResponseDTO toOrderResponseDTO(Order order) {
        List<OrderItemResponseDTO> items = order.getItems() == null ? List.of() :
                order.getItems().stream().map(i ->
                        new OrderItemResponseDTO(i.getId(), i.getProductId(), i.getProductName(),
                                i.getQuantity(), i.getUnitPrice(),
                                i.getUnitPrice() * i.getQuantity(),
                                i.getSize(), i.getFlavor(), i.getToppings(), i.getNotes())
                ).toList();
        return new OrderResponseDTO(order.getId(), order.getCustomerName(), order.getCustomerPhone(),
                order.getStatus(), order.getChannel(), order.getTableNumber(),
                order.getNotes(), order.getTotal(), order.getCreatedAt(), items);
    }
}
