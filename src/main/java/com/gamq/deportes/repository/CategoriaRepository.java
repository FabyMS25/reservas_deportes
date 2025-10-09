package com.gamq.deportes.repository;

import com.gamq.deportes.model.CategoriaDeportiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<CategoriaDeportiva, Long> {

    @Query("SELECT c FROM CategoriaDeportiva c WHERE c.uuid = ?1")
    Optional<CategoriaDeportiva> findByUuid(String uuid);
}
