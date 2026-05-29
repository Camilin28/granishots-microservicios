package com.granishots.notifications.domain.model.gateway;

import com.granishots.notifications.domain.model.NotificationTemplate;
import java.util.List;

public interface TemplateGateway {
    NotificationTemplate save(NotificationTemplate template);
    NotificationTemplate findById(Long id);
    List<NotificationTemplate> findAll();
    List<NotificationTemplate> findByType(String type);
    List<NotificationTemplate> findByChannel(String channel);
    NotificationTemplate update(NotificationTemplate template);
    void deleteById(Long id);
}
