package com.granishots.orders.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderData, Long> {
    List<OrderData> findByStatus(String status);
    List<OrderData> findByCustomerPhone(String phone);

    @Query("SELECT o FROM OrderData o WHERE o.createdAt >= :start AND o.createdAt <= :end")
    List<OrderData> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
