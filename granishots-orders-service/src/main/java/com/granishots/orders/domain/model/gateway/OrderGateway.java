package com.granishots.orders.domain.model.gateway;

import com.granishots.orders.domain.model.Order;
import java.util.List;

public interface OrderGateway {
    Order save(Order order);
    Order findById(Long id);
    List<Order> findAll();
    List<Order> findByStatus(String status);
    List<Order> findByCustomerPhone(String phone);
    List<Order> findToday();
    Order updateStatus(Long id, String status);
    void deleteById(Long id);
}
