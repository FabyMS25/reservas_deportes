package com.gamq.licencias.repository;

import com.gamq.licencias.model.deportes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface deportesRepository extends JpaRepository<deportes, Long> {

    @Query("SELECT c FROM deportes c WHERE c.uuid = ?1")
    Optional<deportes> findByUuid(String uuid);
}
