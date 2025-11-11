package com.gamq.deportes.repository;
import com.gamq.deportes.model.HorarioExcepcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface HorarioExcepcionRepository extends JpaRepository<HorarioExcepcion, Long> {

    @Query("SELECT c FROM HorarioExcepcion c WHERE c.uuid = ?1")
    Optional<HorarioExcepcion> findByUuid(String uuid);

    @Query("SELECT h FROM HorarioExcepcion h WHERE h.campoDeportivo.uuid = ?1")
    List<HorarioExcepcion> findByCampoUuid(@Param("campoUuid") String campoUuid);

    @Query("SELECT h FROM HorarioExcepcion h WHERE h.fecha = :fecha AND h.estado = false")
    List<HorarioExcepcion> findByFecha(@Param("fecha") LocalDate fecha);

    @Query("SELECT h FROM HorarioExcepcion h WHERE h.campoDeportivo.uuid = :campoUuid AND h.fecha = :fecha AND h.estado = false")
    Optional<HorarioExcepcion> findByCampoAndFecha(@Param("campoUuid") String campoUuid, @Param("fecha") LocalDate fecha);

    @Query("SELECT h FROM HorarioExcepcion h WHERE h.fecha BETWEEN :startDate AND :endDate AND h.estado = false")
    List<HorarioExcepcion> findByFechaBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT h FROM HorarioExcepcion h WHERE h.tipoExcepcion = :tipoExcepcion AND h.estado = false")
    List<HorarioExcepcion> findByTipoExcepcion(@Param("tipoExcepcion") String tipoExcepcion);

    @Query("SELECT h FROM HorarioExcepcion h WHERE h.campoDeportivo.uuid = :campoUuid AND h.fecha >= :fecha AND h.estado = false")
    List<HorarioExcepcion> findFuturasByCampo(@Param("campoUuid") String campoUuid, @Param("fecha") LocalDate fecha);

    @Query("SELECT h FROM HorarioExcepcion h WHERE h.estado = false")
    List<HorarioExcepcion> findAllActivos();

    @Query("SELECT COUNT(h) FROM HorarioExcepcion h WHERE h.campoDeportivo.uuid = :campoUuid AND h.estado = false")
    Long countByCampoUuid(@Param("campoUuid") String campoUuid);

    boolean existsByUuidAndEstadoFalse(String uuid);
    boolean existsByCampoDeportivoUuidAndFechaAndEstadoFalse(String campoUuid, LocalDate fecha);
}