package com.gamq.deportes.repository;

import com.gamq.deportes.model.EncargadoComplejo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EncargadoComplejoRepository extends JpaRepository<EncargadoComplejo, Long> {

    @Query("SELECT c FROM EncargadoComplejo c WHERE c.uuid = ?1")
    Optional<EncargadoComplejo> findByUuid(String uuid);
    @Query("SELECT c FROM EncargadoComplejo c WHERE c.userUuid = ?1")
    Optional<EncargadoComplejo> findByUserUuid(String userUuid);

    @Query("SELECT e FROM EncargadoComplejo e WHERE e.complejo.uuid = :complejoUuid AND e.estado = false")
    List<EncargadoComplejo> findByComplejoUuid(@Param("complejoUuid") String complejoUuid);

    @Query("SELECT e FROM EncargadoComplejo e WHERE e.activo = true AND e.estado = false")
    List<EncargadoComplejo> findActivos();

    @Query("SELECT e FROM EncargadoComplejo e WHERE e.complejo.uuid = :complejoUuid AND e.activo = true AND e.estado = false")
    List<EncargadoComplejo> findActivosByComplejo(@Param("complejoUuid") String complejoUuid);

    @Query("SELECT e FROM EncargadoComplejo e WHERE e.estado = false")
    List<EncargadoComplejo> findAllActivos();

    @Query("SELECT COUNT(e) FROM EncargadoComplejo e WHERE e.complejo.uuid = :complejoUuid AND e.estado = false")
    Long countByComplejoUuid(@Param("complejoUuid") String complejoUuid);

    @Query("SELECT e FROM EncargadoComplejo e WHERE e.nombre LIKE %:nombre% AND e.estado = false")
    List<EncargadoComplejo> findByNombreContaining(@Param("nombre") String nombre);

    boolean existsByUuidAndEstadoFalse(String uuid);
    boolean existsByUserUuidAndEstadoFalse(String userUuid);
}