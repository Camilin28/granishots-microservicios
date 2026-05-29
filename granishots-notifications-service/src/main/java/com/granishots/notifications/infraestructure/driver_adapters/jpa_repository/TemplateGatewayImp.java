package com.granishots.notifications.infraestructure.driver_adapters.jpa_repository;

import com.granishots.notifications.domain.model.NotificationTemplate;
import com.granishots.notifications.domain.model.gateway.TemplateGateway;
import com.granishots.notifications.infraestructure.mapper.NotificationMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class TemplateGatewayImp implements TemplateGateway {

    private final TemplateJpaRepository templateJpaRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationTemplate save(NotificationTemplate template) {
        return notificationMapper.toTemplate(
                templateJpaRepository.save(notificationMapper.toTemplateData(template)));
    }

    @Override
    public NotificationTemplate findById(Long id) {
        return templateJpaRepository.findById(id)
                .map(notificationMapper::toTemplate).orElse(null);
    }

    @Override
    public List<NotificationTemplate> findAll() {
        return templateJpaRepository.findAll().stream()
                .map(notificationMapper::toTemplate).toList();
    }

    @Override
    public List<NotificationTemplate> findByType(String type) {
        return templateJpaRepository.findByType(type).stream()
                .map(notificationMapper::toTemplate).toList();
    }

    @Override
    public List<NotificationTemplate> findByChannel(String channel) {
        return templateJpaRepository.findByChannel(channel).stream()
                .map(notificationMapper::toTemplate).toList();
    }

    @Override
    public NotificationTemplate update(NotificationTemplate template) {
        return notificationMapper.toTemplate(
                templateJpaRepository.save(notificationMapper.toTemplateData(template)));
    }

    @Override
    public void deleteById(Long id) {
        templateJpaRepository.deleteById(id);
    }
}
