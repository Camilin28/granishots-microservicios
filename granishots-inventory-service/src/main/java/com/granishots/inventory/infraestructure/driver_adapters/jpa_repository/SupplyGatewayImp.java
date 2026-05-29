package com.granishots.inventory.infraestructure.driver_adapters.jpa_repository;

import com.granishots.inventory.domain.model.Supply;
import com.granishots.inventory.domain.model.gateway.SupplyGateway;
import com.granishots.inventory.infraestructure.mapper.SupplyMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SupplyGatewayImp implements SupplyGateway {

    private final SupplyJpaRepository supplyJpaRepository;
    private final SupplyMapper supplyMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override public Supply save(Supply s) { return supplyMapper.toSupply(supplyJpaRepository.save(supplyMapper.toSupplyData(s))); }
    @Override public Supply findById(Long id) { return supplyJpaRepository.findById(id).map(supplyMapper::toSupply).orElse(null); }
    @Override public List<Supply> findAll() { return supplyJpaRepository.findAll().stream().map(supplyMapper::toSupply).toList(); }
    @Override public List<Supply> findLowStock() { return supplyJpaRepository.findLowStock().stream().map(supplyMapper::toSupply).toList(); }
    @Override public List<Supply> findOutOfStock() { return supplyJpaRepository.findOutOfStock().stream().map(supplyMapper::toSupply).toList(); }
    @Override public List<Supply> findByName(String n) { return supplyJpaRepository.findByNameContainingIgnoreCase(n).stream().map(supplyMapper::toSupply).toList(); }
    @Override public Supply update(Supply s) { return supplyMapper.toSupply(supplyJpaRepository.save(supplyMapper.toSupplyData(s))); }

    @Override
    @Transactional
    public void deleteById(Long id) {
        supplyJpaRepository.deleteById(id);
        supplyJpaRepository.flush();
        entityManager.createNativeQuery(
            "SELECT setval('supplies_id_seq', COALESCE((SELECT MAX(id) FROM supplies), 0))"
        ).getSingleResult();
    }

    @Override public Supply updateMinStock(Long id, Double minStock) {
        SupplyData d = supplyJpaRepository.findById(id).orElseThrow();
        d.setMinStock(minStock);
        return supplyMapper.toSupply(supplyJpaRepository.save(d));
    }

    @Override public Supply addStock(Long id, Double quantity) {
        SupplyData d = supplyJpaRepository.findById(id).orElseThrow();
        d.setCurrentStock(d.getCurrentStock() + quantity);
        return supplyMapper.toSupply(supplyJpaRepository.save(d));
    }

    @Override public Supply subtractStock(Long id, Double quantity) {
        SupplyData d = supplyJpaRepository.findById(id).orElseThrow();
        d.setCurrentStock(d.getCurrentStock() - quantity);
        return supplyMapper.toSupply(supplyJpaRepository.save(d));
    }
}
