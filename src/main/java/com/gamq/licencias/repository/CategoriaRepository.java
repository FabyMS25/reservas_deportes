package com.gamq.licencias.repository;

import com.gamq.licencias.model.Categoria;
import com.gamq.licencias.model.canchas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    @Query("SELECT c FROM Categoria c WHERE c.uuid = ?1")
    Optional<Categoria> findByUuid(String uuid);
}
