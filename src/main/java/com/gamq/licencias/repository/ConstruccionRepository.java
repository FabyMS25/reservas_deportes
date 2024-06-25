package com.gamq.licencias.repository;

import com.gamq.licencias.model.Construccion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConstruccionRepository extends JpaRepository<Construccion, Long> {

    @Query ("SELECT c FROM Construccion c WHERE c.uuid = ?1")
    Optional<Construccion> findByUuid(String uuid);

}
