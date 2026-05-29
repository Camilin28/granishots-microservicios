package com.granishots.login.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserData, Long> {
    Optional<UserData> findByEmail(String email);
    boolean existsByEmail(String email);
}
