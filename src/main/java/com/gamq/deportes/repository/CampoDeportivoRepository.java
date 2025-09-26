package com.gamq.deportes.repository;

import com.gamq.deportes.model.CampoDeportivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
@Repository
public interface CampoDeportivoRepository extends JpaRepository<CampoDeportivo, Long> {

    @Query("SELECT c FROM CampoDeportivo c WHERE c.uuid = ?1")
    Optional<CampoDeportivo> findByUuid(String uuid);

    @Query("SELECT c FROM CampoDeportivo c WHERE c.complejo.uuid = ?1")
//    @Query("SELECT c FROM CampoDeportivo c WHERE c.complejo.uuid = :complejoUuid AND c.estado = false")
    List<CampoDeportivo> findByComplejoUuid(String complejoUuid);

//    @Query("SELECT c FROM campoDeportivo c WHERE c.superficie.uuid = ?1")
    @Query("SELECT c FROM CampoDeportivo c WHERE c.superficie.uuid = :superficieUuid AND c.estado = false")
    List<CampoDeportivo> findBySuperficieUuid( String superficieUuid);

    @Query("SELECT c FROM CampoDeportivo c WHERE c.estado = false")
    List<CampoDeportivo> findAllActivos();

    @Query("SELECT c FROM CampoDeportivo c WHERE c.estadoActual = :estadoActual AND c.estado = false")
    List<CampoDeportivo> findByEstadoActual(@Param("estadoActual") String estadoActual);

    @Query("SELECT c FROM CampoDeportivo c WHERE c.codigo = :codigo AND c.estado = false")
    Optional<CampoDeportivo> findByCodigo(@Param("codigo") String codigo);

    @Query("SELECT c FROM CampoDeportivo c WHERE c.complejo.uuid = :complejoUuid AND c.codigo = :codigo AND c.estado = false")
    Optional<CampoDeportivo> findByComplejoAndCodigo(@Param("complejoUuid") String complejoUuid, @Param("codigo") String codigo);

    @Query("SELECT COUNT(c) FROM CampoDeportivo c WHERE c.complejo.uuid = :complejoUuid AND c.estado = false")
    Long countByComplejoUuid(@Param("complejoUuid") String complejoUuid);

//    boolean existsByUuidAndEstadoFalse(String uuid);
//    boolean existsByCodigoAndEstadoFalse(String codigo);
}
