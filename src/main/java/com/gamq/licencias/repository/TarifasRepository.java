package com.gamq.licencias.repository;


import com.gamq.licencias.model.Tarifas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TarifasRepository extends JpaRepository<Tarifas, Long> {
    @Query("SELECT c FROM Tarifas c WHERE c.uuid = ?1")
    Optional<Tarifas> findByUuid(String uuid);
}
