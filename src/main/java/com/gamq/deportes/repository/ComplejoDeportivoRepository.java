package com.gamq.deportes.repository;

import com.gamq.deportes.model.ComplejoDeportivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplejoDeportivoRepository extends JpaRepository<ComplejoDeportivo, Long> {
    @Query("SELECT c FROM ComplejoDeportivo c WHERE c.uuid = ?1")
    Optional<ComplejoDeportivo> findByUuid(String uuid);

    @Query("SELECT c FROM ComplejoDeportivo c WHERE c.estado = false")
    List<ComplejoDeportivo> findAllActivos();

    @Query("SELECT c FROM ComplejoDeportivo c WHERE c.estadoActual = :estadoActual AND c.estado = false")
    List<ComplejoDeportivo> findByEstadoActual(@Param("estadoActual") String estadoActual);

    @Query("SELECT c FROM ComplejoDeportivo c WHERE c.distrito = :distrito AND c.estado = false")
    List<ComplejoDeportivo> findByDistrito(@Param("distrito") String distrito);

    @Query("SELECT c FROM ComplejoDeportivo c WHERE c.tipoEstructura = :tipoEstructura AND c.estado = false")
    List<ComplejoDeportivo> findByTipoEstructura(@Param("tipoEstructura") String tipoEstructura);

    @Query("SELECT COUNT(c) FROM ComplejoDeportivo c WHERE c.estado = false")
    Long countActivos();

//    boolean existsByUuidAndEstadoFalse(String uuid);
}
