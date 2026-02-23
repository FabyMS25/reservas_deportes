package com.gamq.deportes.repository;

import com.gamq.deportes.model.ReservaRecurrente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRecurrenteRepository extends JpaRepository<ReservaRecurrente, Long> ,
        JpaSpecificationExecutor<ReservaRecurrente> {

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.tipoRecurrencia = :tipoRecurrencia")
    Page<ReservaRecurrente> findByTipoRecurrencia(@Param("tipoRecurrencia") String tipoRecurrencia, Pageable pageable);
    @Query("SELECT rp FROM ReservaRecurrente rp WHERE rp.reserva.fechaCreacion < :expirationThreshold " +
            "AND rp.reserva.estadoReserva = :estadoReserva")
    List<ReservaRecurrente> findExpiredPendingReservations(
            @Param("expirationThreshold") LocalDateTime expirationThreshold,
            @Param("estadoReserva") String estadoReserva);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.uuid = ?1")
    Optional<ReservaRecurrente> findByUuid(String uuid);

    // Date range queries
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.fechaInicio = :fechaInicio")
    List<ReservaRecurrente> findByFechaInicio(@Param("fechaInicio") LocalDate fechaInicio);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.fechaFin = :fechaFin")
    List<ReservaRecurrente> findByFechaFin(@Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaRecurrente> findByFechaInicioBetween(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.fechaFin BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaRecurrente> findByFechaFinBetween(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaRecurrente> findByFechaCreacionBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Recurrence type and status queries
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.tipoRecurrencia = :tipoRecurrencia")
    List<ReservaRecurrente> findByTipoRecurrencia(@Param("tipoRecurrencia") String tipoRecurrencia);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.activo = :activo")
    List<ReservaRecurrente> findByActivo(@Param("activo") Boolean activo);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.tipoRecurrencia = :tipoRecurrencia AND rr.activo = :activo")
    List<ReservaRecurrente> findByTipoRecurrenciaAndActivo(
            @Param("tipoRecurrencia") String tipoRecurrencia,
            @Param("activo") Boolean activo
    );

    // Capacity and duration queries
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.cantidadPersonas = :cantidadPersonas")
    List<ReservaRecurrente> findByCantidadPersonas(@Param("cantidadPersonas") Integer cantidadPersonas);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.cantidadPersonas BETWEEN :cantidadMin AND :cantidadMax")
    List<ReservaRecurrente> findByCantidadPersonasBetween(
            @Param("cantidadMin") Integer cantidadMin,
            @Param("cantidadMax") Integer cantidadMax
    );

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.duracionSesionHoras = :duracionHoras")
    List<ReservaRecurrente> findByDuracionSesionHoras(@Param("duracionHoras") Integer duracionHoras);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.duracionSesionHoras BETWEEN :duracionMin AND :duracionMax")
    List<ReservaRecurrente> findByDuracionSesionHorasBetween(
            @Param("duracionMin") Integer duracionMin,
            @Param("duracionMax") Integer duracionMax
    );

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.cantidadSesiones = :cantidadSesiones")
    List<ReservaRecurrente> findByCantidadSesiones(@Param("cantidadSesiones") Integer cantidadSesiones);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.cantidadSesiones BETWEEN :sesionesMin AND :sesionesMax")
    List<ReservaRecurrente> findByCantidadSesionesBetween(
            @Param("sesionesMin") Integer sesionesMin,
            @Param("sesionesMax") Integer sesionesMax
    );

    // Days of week queries - REMOVED BITMASK OPERATIONS
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.diasSemana = :diasSemana")
    List<ReservaRecurrente> findByDiasSemana(@Param("diasSemana") Integer diasSemana);

    // Tarifa queries
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.tarifaMensual = :tarifaMensual")
    List<ReservaRecurrente> findByTarifaMensual(@Param("tarifaMensual") Integer tarifaMensual);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.tarifaMensual BETWEEN :tarifaMin AND :tarifaMax")
    List<ReservaRecurrente> findByTarifaMensualBetween(
            @Param("tarifaMin") Integer tarifaMin,
            @Param("tarifaMax") Integer tarifaMax
    );

    // Category and surface queries
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.categoria = :categoria")
    List<ReservaRecurrente> findByCategoria(@Param("categoria") String categoria);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.superficie = :superficie")
    List<ReservaRecurrente> findBySuperficie(@Param("superficie") String superficie);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.categoria = :categoria AND rr.superficie = :superficie")
    List<ReservaRecurrente> findByCategoriaAndSuperficie(
            @Param("categoria") String categoria,
            @Param("superficie") String superficie
    );

    // Relationship-based queries
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.reserva.uuid = :reservaUuid")
    Optional<ReservaRecurrente> findByReservaUuid(@Param("reservaUuid") String reservaUuid);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.reserva.idReserva = :idReserva")
    Optional<ReservaRecurrente> findByReservaId(@Param("idReserva") Long idReserva);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.reserva.campoDeportivo.uuid = :campoUuid")
    List<ReservaRecurrente> findByCampoDeportivoUuid(@Param("campoUuid") String campoUuid);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.reserva.contribuyente.uuid = :contribuyenteUuid")
    List<ReservaRecurrente> findByContribuyenteUuid(@Param("contribuyenteUuid") String contribuyenteUuid);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.tablero.uuid = :tableroUuid")
    List<ReservaRecurrente> findByTableroUuid(@Param("tableroUuid") String tableroUuid);

    // Combined filters with relationships
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.reserva.campoDeportivo.uuid = :campoUuid AND rr.activo = true")
    List<ReservaRecurrente> findActivasByCampoDeportivo(@Param("campoUuid") String campoUuid);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.reserva.campoDeportivo.uuid = :campoUuid AND rr.tipoRecurrencia = :tipoRecurrencia")
    List<ReservaRecurrente> findByCampoDeportivoAndTipoRecurrencia(
            @Param("campoUuid") String campoUuid,
            @Param("tipoRecurrencia") String tipoRecurrencia
    );

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.reserva.estadoReserva = :estadoReserva")
    List<ReservaRecurrente> findByReservaEstadoReserva(@Param("estadoReserva") String estadoReserva);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.reserva.estadoReserva IN :estadosReserva")
    List<ReservaRecurrente> findByReservaEstadoReservaIn(@Param("estadosReserva") List<String> estadosReserva);

    // Complex business queries for active recurrent reservations
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.activo = true " +
            "AND rr.reserva.estadoReserva IN ('APROBADA', 'CONFIRMADA', 'ACTIVA') " +
            "AND rr.fechaInicio <= :fecha AND (rr.fechaFin IS NULL OR rr.fechaFin >= :fecha)")
    List<ReservaRecurrente> findActivasEnFecha(@Param("fecha") LocalDate fecha);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.activo = true " +
            "AND rr.reserva.estadoReserva IN ('APROBADA', 'CONFIRMADA', 'ACTIVA') " +
            "AND rr.reserva.campoDeportivo.uuid = :campoUuid " +
            "AND rr.fechaInicio <= :fecha AND (rr.fechaFin IS NULL OR rr.fechaFin >= :fecha)")
    List<ReservaRecurrente> findActivasEnFechaPorCampo(
            @Param("campoUuid") String campoUuid,
            @Param("fecha") LocalDate fecha
    );

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.activo = true " +
            "AND rr.reserva.estadoReserva IN ('APROBADA', 'CONFIRMADA', 'ACTIVA') " +
            "AND (rr.fechaFin IS NULL OR rr.fechaFin >= CURRENT_DATE)")
    List<ReservaRecurrente> findActivasVigentes();

    // REMOVED: findActivasPorDiaSemana - bitmask operations not supported in JPQL

    // Paginated queries
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.reserva.campoDeportivo.uuid = :campoUuid")
    Page<ReservaRecurrente> findByCampoDeportivoUuid(@Param("campoUuid") String campoUuid, Pageable pageable);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.activo = :activo")
    Page<ReservaRecurrente> findByActivo(@Param("activo") Boolean activo, Pageable pageable);

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    Page<ReservaRecurrente> findByFechaInicioBetween(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            Pageable pageable
    );

    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.reserva.estadoReserva = :estadoReserva")
    Page<ReservaRecurrente> findByReservaEstadoReserva(@Param("estadoReserva") String estadoReserva, Pageable pageable);

    // Count queries
    @Query("SELECT COUNT(rr) FROM ReservaRecurrente rr WHERE rr.reserva.campoDeportivo.uuid = :campoUuid AND rr.activo = true")
    Long countActivasByCampoDeportivo(@Param("campoUuid") String campoUuid);

    @Query("SELECT COUNT(rr) FROM ReservaRecurrente rr WHERE rr.tipoRecurrencia = :tipoRecurrencia AND rr.activo = true")
    Long countActivasByTipoRecurrencia(@Param("tipoRecurrencia") String tipoRecurrencia);

    @Query("SELECT COUNT(rr) FROM ReservaRecurrente rr WHERE rr.activo = true AND rr.fechaInicio <= CURRENT_DATE AND (rr.fechaFin IS NULL OR rr.fechaFin >= CURRENT_DATE)")
    Long countActivasVigentes();

    @Query("SELECT COUNT(rr) FROM ReservaRecurrente rr WHERE rr.categoria = :categoria")
    Long countByCategoria(@Param("categoria") String categoria);

    @Query("SELECT COUNT(rr) FROM ReservaRecurrente rr WHERE rr.superficie = :superficie")
    Long countBySuperficie(@Param("superficie") String superficie);

    // Analytics and reporting queries
    @Query("SELECT rr.tipoRecurrencia, COUNT(rr) FROM ReservaRecurrente rr GROUP BY rr.tipoRecurrencia")
    List<Object[]> countByTipoRecurrenciaGroup();

    @Query("SELECT rr.categoria, COUNT(rr) FROM ReservaRecurrente rr GROUP BY rr.categoria")
    List<Object[]> countByCategoriaGroup();

    @Query("SELECT rr.superficie, COUNT(rr) FROM ReservaRecurrente rr GROUP BY rr.superficie")
    List<Object[]> countBySuperficieGroup();

    @Query("SELECT rr.activo, COUNT(rr) FROM ReservaRecurrente rr GROUP BY rr.activo")
    List<Object[]> countByActivoGroup();

    @Query("SELECT rr.reserva.campoDeportivo.nombre, COUNT(rr) FROM ReservaRecurrente rr GROUP BY rr.reserva.campoDeportivo.nombre, rr.reserva.campoDeportivo.uuid")
    List<Object[]> countByCampoDeportivoGroup();

    // Expiring soon queries
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.activo = true " +
            "AND rr.fechaFin IS NOT NULL " +
            "AND rr.fechaFin BETWEEN CURRENT_DATE AND :fechaLimite")
    List<ReservaRecurrente> findPorVencer(@Param("fechaLimite") LocalDate fechaLimite);

    // Find reservations with observations
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.observaciones IS NOT NULL AND rr.observaciones != ''")
    List<ReservaRecurrente> findWithObservaciones();

    // Find without tablero assigned
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.tablero IS NULL")
    List<ReservaRecurrente> findSinTablero();

    // Find by monthly fee range and active status
    @Query("SELECT rr FROM ReservaRecurrente rr WHERE rr.activo = true " +
            "AND rr.tarifaMensual BETWEEN :tarifaMin AND :tarifaMax " +
            "AND rr.reserva.estadoReserva IN ('APROBADA', 'CONFIRMADA', 'ACTIVA')")
    List<ReservaRecurrente> findActivasByTarifaMensualBetween(
            @Param("tarifaMin") Integer tarifaMin,
            @Param("tarifaMax") Integer tarifaMax
    );
}