package com.gamq.licencias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface canchaDeportesRepository extends JpaRepository<canchaDeportes,Long> {
    @Query("SELECT c FROM canchaDeportes c WHERE c.uuid = ?1")
    Optional<canchaDeportes> findByUuid(String uuid);
}
