package com.granishots.notifications.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateJpaRepository extends JpaRepository<TemplateData, Long> {
    List<TemplateData> findByType(String type);
    List<TemplateData> findByChannel(String channel);
    List<TemplateData> findByActiveTrue();
}
