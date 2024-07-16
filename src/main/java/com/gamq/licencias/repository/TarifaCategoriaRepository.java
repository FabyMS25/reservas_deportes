package com.gamq.licencias.repository;

import com.gamq.licencias.model.TarifaCategoria;
import com.gamq.licencias.model.Tarifas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TarifaCategoriaRepository extends JpaRepository<TarifaCategoria, Long> {

    @Query("SELECT c FROM tarifaCategoria c WHERE c.uuid = ?1")
    Optional<TarifaCategoria> findByUuid(String uuid);
}
