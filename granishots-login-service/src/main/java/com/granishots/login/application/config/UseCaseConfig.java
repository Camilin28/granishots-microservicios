package com.granishots.login.application.config;

import com.granishots.login.domain.model.gateway.EncrypterGateway;
import com.granishots.login.domain.model.gateway.UserGateway;
import com.granishots.login.domain.usecase.UserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public UserUseCase userUseCase(UserGateway userGateway, EncrypterGateway encrypterGateway) {
        return new UserUseCase(userGateway, encrypterGateway);
    }
}
