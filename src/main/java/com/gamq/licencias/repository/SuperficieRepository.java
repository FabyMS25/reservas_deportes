package com.gamq.licencias.repository;

import com.gamq.licencias.model.Superficie;
import com.gamq.licencias.model.canchas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SuperficieRepository extends JpaRepository<Superficie, Long> {

    @Query("SELECT c FROM superficie c WHERE c.uuid = ?1")
    Optional<Superficie> findByUuid(String uuid);
}
