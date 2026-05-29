package com.granishots.login.domain.model.gateway;

import com.granishots.login.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserGateway {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    User update(User user);
    void deleteById(Long id);
    User updateStatus(Long id, Boolean active);
}
