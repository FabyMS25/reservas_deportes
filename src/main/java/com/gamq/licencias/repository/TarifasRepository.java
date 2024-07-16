package com.gamq.licencias.repository;

import com.gamq.licencias.dto.TarifaDto;
import com.gamq.licencias.model.Superficie;
import com.gamq.licencias.model.Tarifas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TarifasRepository extends JpaRepository<Tarifas, Long> {
    @Query("SELECT c FROM tarifas c WHERE c.uuid = ?1")
    Optional<Tarifas> findByUuid(String uuid);
}
