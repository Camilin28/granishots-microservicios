package com.granishots.inventory.application.config;

import com.granishots.inventory.domain.model.gateway.SupplyGateway;
import com.granishots.inventory.domain.usecase.SupplyUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public SupplyUseCase supplyUseCase(SupplyGateway supplyGateway) {
        return new SupplyUseCase(supplyGateway);
    }
}
