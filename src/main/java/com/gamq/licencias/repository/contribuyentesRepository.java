package com.gamq.licencias.repository;

import com.gamq.licencias.model.contribuyentes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface contribuyentesRepository extends JpaRepository<contribuyentes, Long> {

    @Query("SELECT c FROM contribuyentes c WHERE c.uuid = ?1")
    Optional<contribuyentes> findByUuid(String uuid);
}
