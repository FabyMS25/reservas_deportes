package com.gamq.licencias.repository;

import com.gamq.licencias.model.encargadoCancha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface encargadoCanchaRepository extends JpaRepository<encargadoCancha, Long> {

    @Query("SELECT c FROM encargadoCancha c WHERE c.uuid = ?1")
    Optional<encargadoCancha> findByUuid(String uuid);
}
