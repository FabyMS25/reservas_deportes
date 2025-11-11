package com.gamq.deportes.repository;
import com.gamq.deportes.model.HorarioFijo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioFijoRepository extends JpaRepository<HorarioFijo, Long> {

    @Query("SELECT c FROM HorarioFijo c WHERE c.uuid = ?1")
    Optional<HorarioFijo> findByUuid(String uuid);

    @Query("SELECT h FROM HorarioFijo h WHERE h.campoDeportivo.uuid = ?1")
    List<HorarioFijo> findByCampoUuid(@Param("campoUuid") String campoUuid);

    @Query("SELECT h FROM HorarioFijo h WHERE h.dia = :dia AND h.estado = false")
    List<HorarioFijo> findByDia(@Param("dia") String dia);

    @Query("SELECT h FROM HorarioFijo h WHERE h.campoDeportivo.uuid = :campoUuid AND h.dia = :dia AND h.estado = false")
    List<HorarioFijo> findByCampoAndDia(@Param("campoUuid") String campoUuid, @Param("dia") String dia);

    @Query("SELECT h FROM HorarioFijo h WHERE h.abierto = true AND h.estado = false")
    List<HorarioFijo> findAbiertos();

    @Query("SELECT h FROM HorarioFijo h WHERE h.campoDeportivo.uuid = :campoUuid AND h.abierto = true AND h.estado = false")
    List<HorarioFijo> findAbiertosByCampo(@Param("campoUuid") String campoUuid);

    @Query("SELECT h FROM HorarioFijo h WHERE h.estado = false")
    List<HorarioFijo> findAllActivos();

    @Query("SELECT COUNT(h) FROM HorarioFijo h WHERE h.campoDeportivo.uuid = :campoUuid AND h.estado = false")
    Long countByCampoUuid(@Param("campoUuid") String campoUuid);

    boolean existsByUuidAndEstadoFalse(String uuid);
}