package com.granishots.orders.application.config;

import com.granishots.orders.domain.model.gateway.OrderGateway;
import com.granishots.orders.domain.usecase.OrderUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public OrderUseCase orderUseCase(OrderGateway orderGateway) {
        return new OrderUseCase(orderGateway);
    }
}
