package com.granishots.catalog.application.config;

import com.granishots.catalog.domain.model.gateway.ProductGateway;
import com.granishots.catalog.domain.usecase.ProductUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ProductUseCase productUseCase(ProductGateway productGateway) {
        return new ProductUseCase(productGateway);
    }
}
