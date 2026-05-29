package com.granishots.login.infraestructure.driver_adapters.jpa_repository;

import com.granishots.login.domain.model.User;
import com.granishots.login.domain.model.gateway.UserGateway;
import com.granishots.login.infraestructure.mapper.UserMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserGatewayImp implements UserGateway {

    private final UserJpaRepository repo;
    private final UserMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User save(User u) {
        return mapper.toUser(repo.save(mapper.toUserData(u)));
    }

    @Override
    public Optional<User> findById(Long id) {
        return repo.findById(id).map(mapper::toUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email).map(mapper::toUser);
    }

    @Override
    public List<User> findAll() {
        return repo.findAll().stream().map(mapper::toUser).toList();
    }

    @Override
    public User update(User u) {
        return mapper.toUser(repo.save(mapper.toUserData(u)));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repo.deleteById(id);
        repo.flush();
        // Resetea la secuencia al valor máximo actual de ID
        // Si la tabla queda vacía, el próximo ID será 1
        entityManager.createNativeQuery(
            "SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 0))"
        ).getSingleResult();
    }

    @Override
    public User updateStatus(Long id, Boolean active) {
        UserData d = repo.findById(id).orElseThrow();
        d.setActive(active);
        return mapper.toUser(repo.save(d));
    }
}
