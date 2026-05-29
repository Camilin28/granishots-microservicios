package com.granishots.orders.infraestructure.driver_adapters.jpa_repository;

import com.granishots.orders.domain.model.Order;
import com.granishots.orders.domain.model.gateway.OrderGateway;
import com.granishots.orders.infraestructure.mapper.OrderMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderGatewayImp implements OrderGateway {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderMapper orderMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override public Order save(Order o) { return orderMapper.toOrder(orderJpaRepository.save(orderMapper.toOrderData(o))); }
    @Override public Order findById(Long id) { return orderJpaRepository.findById(id).map(orderMapper::toOrder).orElse(null); }
    @Override public List<Order> findAll() { return orderJpaRepository.findAll().stream().map(orderMapper::toOrder).toList(); }
    @Override public List<Order> findByStatus(String s) { return orderJpaRepository.findByStatus(s).stream().map(orderMapper::toOrder).toList(); }
    @Override public List<Order> findByCustomerPhone(String p) { return orderJpaRepository.findByCustomerPhone(p).stream().map(orderMapper::toOrder).toList(); }

    @Override public List<Order> findToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return orderJpaRepository.findByCreatedAtBetween(start, end).stream().map(orderMapper::toOrder).toList();
    }

    @Override public Order updateStatus(Long id, String status) {
        OrderData d = orderJpaRepository.findById(id).orElseThrow();
        d.setStatus(status);
        return orderMapper.toOrder(orderJpaRepository.save(d));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        orderJpaRepository.deleteById(id);
        orderJpaRepository.flush();
        entityManager.createNativeQuery(
            "SELECT setval('orders_id_seq', COALESCE((SELECT MAX(id) FROM orders), 0))"
        ).getSingleResult();
    }
}
