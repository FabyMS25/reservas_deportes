package com.gamq.deportes.repository;

import com.gamq.deportes.model.SistemaAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SistemaAuditoriaRepository extends JpaRepository<SistemaAuditoria, Long> {

    @Query("SELECT sa FROM SistemaAuditoria sa WHERE sa.uuid = ?1")
    Optional<SistemaAuditoria> findByUuid(String uuid);

    @Query("SELECT sa FROM SistemaAuditoria sa WHERE sa.usuarioUuid = :usuarioUuid ORDER BY sa.fechaAccion DESC")
    List<SistemaAuditoria> findByUsuarioUuid(@Param("usuarioUuid") String usuarioUuid);

    @Query("SELECT sa FROM SistemaAuditoria sa WHERE sa.accion = :accion ORDER BY sa.fechaAccion DESC")
    List<SistemaAuditoria> findByAccion(@Param("accion") String accion);

    @Query("SELECT sa FROM SistemaAuditoria sa WHERE sa.modulo = :modulo ORDER BY sa.fechaAccion DESC")
    List<SistemaAuditoria> findByModulo(@Param("modulo") String modulo);

    @Query("SELECT sa FROM SistemaAuditoria sa WHERE sa.entidadTipo = :entidadTipo AND sa.entidadUuid = :entidadUuid ORDER BY sa.fechaAccion DESC")
    List<SistemaAuditoria> findByEntidad(
            @Param("entidadTipo") String entidadTipo,
            @Param("entidadUuid") String entidadUuid
    );

    @Query("SELECT sa FROM SistemaAuditoria sa WHERE sa.fechaAccion BETWEEN :fechaInicio AND :fechaFin ORDER BY sa.fechaAccion DESC")
    List<SistemaAuditoria> findByFechaAccionBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT sa FROM SistemaAuditoria sa WHERE sa.fechaAccion BETWEEN :fechaInicio AND :fechaFin ORDER BY sa.fechaAccion DESC")
    Page<SistemaAuditoria> findByFechaAccionBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );

    @Query("SELECT sa.accion, COUNT(sa) FROM SistemaAuditoria sa GROUP BY sa.accion")
    List<Object[]> countByAccionGroup();

    @Query("SELECT sa.modulo, COUNT(sa) FROM SistemaAuditoria sa GROUP BY sa.modulo")
    List<Object[]> countByModuloGroup();

    @Query("SELECT sa.usuarioUuid, COUNT(sa) FROM SistemaAuditoria sa GROUP BY sa.usuarioUuid")
    List<Object[]> countByUsuarioUuidGroup();
}
