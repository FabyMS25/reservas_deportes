package com.gamq.licencias.repository;

import com.gamq.licencias.model.Categoria;
import com.gamq.licencias.model.complejo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface complejoRepository extends JpaRepository<complejo, Long> {
    @Query("SELECT c FROM complejo c WHERE c.uuid = ?1")
    Optional<complejo> findByUuid(String uuid);
}
