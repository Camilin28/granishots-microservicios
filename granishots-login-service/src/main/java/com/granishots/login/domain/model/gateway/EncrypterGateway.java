package com.granishots.login.domain.model.gateway;

public interface EncrypterGateway {
    String encrypt(String rawPassword);
    boolean matches(String rawPassword, String encryptedPassword);
}
