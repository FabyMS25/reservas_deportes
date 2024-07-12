package com.gamq.licencias.repository;

import com.gamq.licencias.model.encargadoComplejo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface encargadoCanchaRepository extends JpaRepository<encargadoComplejo, Long> {

    @Query("SELECT c FROM encargadoCancha c WHERE c.uuid = ?1")
    Optional<encargadoComplejo> findByUuid(String uuid);
}
