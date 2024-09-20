package com.gamq.deportes.repository;

import com.gamq.deportes.model.TarifaCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TarifaCategoriaRepository extends JpaRepository<TarifaCategoria, Long> {

    @Query("SELECT c FROM TarifaCategoria c WHERE c.uuid = ?1")
    Optional<TarifaCategoria> findByUuid(String uuid);

    @Query("SELECT c FROM TarifaCategoria c WHERE c.categoria.uuid = ?1")
    List<TarifaCategoria> findByCategory(String uuidCategoria);
}
