package com.gamq.deportes.repository;


import com.gamq.deportes.model.Tarifas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarifasRepository extends JpaRepository<Tarifas, Long> {
    @Query("SELECT c FROM Tarifas c WHERE c.uuid = ?1")
    Optional<Tarifas> findByUuid(String uuid);

    @Query("SELECT t FROM Tarifas t WHERE t.cancha.uuid = ?1")
    List<Tarifas> findTarifasByCanchaUuid(String canchaUuid);

}
