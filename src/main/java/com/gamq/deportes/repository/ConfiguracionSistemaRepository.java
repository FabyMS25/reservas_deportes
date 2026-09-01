package com.gamq.deportes.repository;

import com.gamq.deportes.model.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionSistemaRepository extends JpaRepository<ConfiguracionSistema, Long> {

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.uuid = ?1")
    Optional<ConfiguracionSistema> findByUuid(String uuid);

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.clave = ?1 AND c.aplicaA = 'global' AND c.referenciaUuid IS NULL")
    Optional<ConfiguracionSistema> findByClave(String clave);

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.clave = :clave AND c.aplicaA = 'global' AND c.referenciaUuid IS NULL AND c.estado = false")
    Optional<ConfiguracionSistema> findByClaveAndEstadoFalse(@Param("clave") String clave);

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.clave = :clave AND c.estado = false AND c.activo = true " +
            "AND ((c.aplicaA = :aplicaA AND c.referenciaUuid = :referenciaUuid) " +
            "OR (c.aplicaA = :aplicaA AND c.referenciaUuid IS NULL) " +
            "OR (c.aplicaA = 'global' AND c.referenciaUuid IS NULL)) " +
            "ORDER BY CASE WHEN c.aplicaA = :aplicaA AND c.referenciaUuid = :referenciaUuid THEN 0 " +
            "WHEN c.aplicaA = :aplicaA AND c.referenciaUuid IS NULL THEN 1 ELSE 2 END")
    List<ConfiguracionSistema> findEffectiveCandidates(
            @Param("clave") String clave,
            @Param("aplicaA") String aplicaA,
            @Param("referenciaUuid") String referenciaUuid);

    @Query("SELECT COUNT(c) FROM ConfiguracionSistema c WHERE c.clave = :clave AND c.aplicaA = :aplicaA " +
            "AND ((:referenciaUuid IS NULL AND c.referenciaUuid IS NULL) OR c.referenciaUuid = :referenciaUuid) " +
            "AND c.estado = false")
    Long countByClaveAndScope(
            @Param("clave") String clave,
            @Param("aplicaA") String aplicaA,
            @Param("referenciaUuid") String referenciaUuid);

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.categoria = ?1")
    List<ConfiguracionSistema> findByCategoria(String categoria);

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.categoria = :categoria AND c.estado = false")
    List<ConfiguracionSistema> findByCategoriaAndEstadoFalse(@Param("categoria") String categoria);

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.aplicaA = ?1")
    List<ConfiguracionSistema> findByAplicaA(String aplicaA);

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.aplicaA = :aplicaA AND c.estado = false")
    List<ConfiguracionSistema> findByAplicaAAndEstadoFalse(@Param("aplicaA") String aplicaA);

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.referenciaUuid = ?1")
    List<ConfiguracionSistema> findByReferenciaUuid(String referenciaUuid);

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.referenciaUuid = :referenciaUuid AND c.estado = false")
    List<ConfiguracionSistema> findByReferenciaUuidAndEstadoFalse(@Param("referenciaUuid") String referenciaUuid);

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.activo = true AND c.estado = false")
    List<ConfiguracionSistema> findAllActivas();

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.modificablePorUsuario = true AND c.activo = true AND c.estado = false")
    List<ConfiguracionSistema> findModificablesPorUsuario();

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.requiereAprobacion = true AND c.estado = false")
    List<ConfiguracionSistema> findRequierenAprobacion();

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.categoria = :categoria AND c.activo = true AND c.estado = false")
    List<ConfiguracionSistema> findByCategoriaAndActiva(@Param("categoria") String categoria);

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.clave IN :claves AND c.estado = false")
    List<ConfiguracionSistema> findByClaves(@Param("claves") List<String> claves);

    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.aplicaA = :aplicaA AND c.referenciaUuid = :referenciaUuid AND c.estado = false")
    List<ConfiguracionSistema> findByAplicaAAndReferenciaUuid(@Param("aplicaA") String aplicaA, @Param("referenciaUuid") String referenciaUuid);

    @Query("SELECT COUNT(c) FROM ConfiguracionSistema c WHERE c.clave = :clave AND c.estado = false")
    Long countByClave(@Param("clave") String clave);

    @Query("SELECT COUNT(c) FROM ConfiguracionSistema c WHERE c.categoria = :categoria AND c.estado = false")
    Long countByCategoria(@Param("categoria") String categoria);

    @Query("SELECT DISTINCT c.categoria FROM ConfiguracionSistema c WHERE c.estado = false ORDER BY c.categoria")
    List<String> findDistinctCategorias();

    // Check if configuration exists and is active
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ConfiguracionSistema c WHERE c.clave = :clave AND c.activo = true AND c.estado = false")
    boolean existsByClaveAndActiva(@Param("clave") String clave);

    // Find configurations by multiple criteria
    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.categoria = :categoria AND c.aplicaA = :aplicaA AND c.activo = true AND c.estado = false")
    List<ConfiguracionSistema> findByCategoriaAndAplicaAAndActiva(
            @Param("categoria") String categoria,
            @Param("aplicaA") String aplicaA);
}
