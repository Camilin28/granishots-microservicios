package com.granishots.notifications.application.config;

import com.granishots.notifications.domain.model.gateway.TemplateGateway;
import com.granishots.notifications.domain.usecase.NotificationUseCase;
import com.granishots.notifications.domain.usecase.TemplateUseCase;
import com.granishots.notifications.infraestructure.twilio.NotificationDispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public NotificationUseCase notificationUseCase(NotificationDispatcher notificationDispatcher) {
        return new NotificationUseCase(notificationDispatcher);
    }

    @Bean
    public TemplateUseCase templateUseCase(TemplateGateway templateGateway) {
        return new TemplateUseCase(templateGateway);
    }
}
