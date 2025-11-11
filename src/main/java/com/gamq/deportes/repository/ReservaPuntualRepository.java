package com.gamq.deportes.repository;

import com.gamq.deportes.model.ReservaPuntual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaPuntualRepository extends JpaRepository<ReservaPuntual,Long> {

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.fechaCreacion < :expirationThreshold " +
            "AND rp.reserva.estadoReserva = :estadoReserva")
    List<ReservaPuntual> findExpiredPendingReservations(
            @Param("expirationThreshold") LocalDateTime expirationThreshold,
            @Param("estadoReserva") String estadoReserva);

    @Query("SELECT c FROM ReservaPuntual c WHERE c.uuid = ?1")
    Optional<ReservaPuntual> findByUuid(String uuid);

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.fechaUso = :fechaUso")
    List<ReservaPuntual> findByFechaUso(@Param("fechaUso") LocalDate fechaUso);

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.fechaUso BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaPuntual> findByFechaUsoBetween(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaPuntual> findByFechaCreacionBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.fechaUsoReal BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaPuntual> findByFechaUsoRealBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Time-based queries
    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.horaInicio = :horaInicio")
    List<ReservaPuntual> findByHoraInicio(@Param("horaInicio") LocalTime horaInicio);

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.horaFin = :horaFin")
    List<ReservaPuntual> findByHoraFin(@Param("horaFin") LocalTime horaFin);

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.horaInicio BETWEEN :horaInicio AND :horaFin")
    List<ReservaPuntual> findByHoraInicioBetween(
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    // Duration and capacity queries
    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.duracionHoras = :duracionHoras")
    List<ReservaPuntual> findByDuracionHoras(@Param("duracionHoras") Integer duracionHoras);

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.duracionHoras BETWEEN :duracionMin AND :duracionMax")
    List<ReservaPuntual> findByDuracionHorasBetween(
            @Param("duracionMin") Integer duracionMin,
            @Param("duracionMax") Integer duracionMax
    );

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.cantidadPersonas = :cantidadPersonas")
    List<ReservaPuntual> findByCantidadPersonas(@Param("cantidadPersonas") Integer cantidadPersonas);

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.cantidadPersonas BETWEEN :cantidadMin AND :cantidadMax")
    List<ReservaPuntual> findByCantidadPersonasBetween(
            @Param("cantidadMin") Integer cantidadMin,
            @Param("cantidadMax") Integer cantidadMax
    );

    // Category and surface queries
    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.categoria = :categoria")
    List<ReservaPuntual> findByCategoria(@Param("categoria") String categoria);

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.superficie = :superficie")
    List<ReservaPuntual> findBySuperficie(@Param("superficie") String superficie);

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.categoria = :categoria AND rp.superficie = :superficie")
    List<ReservaPuntual> findByCategoriaAndSuperficie(
            @Param("categoria") String categoria,
            @Param("superficie") String superficie
    );

    // Relationship-based queries
    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.uuid = :reservaUuid")
    Optional<ReservaPuntual> findByReservaUuid(@Param("reservaUuid") String reservaUuid);

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.campoDeportivo.uuid = :campoUuid")
    List<ReservaPuntual> findByCampoDeportivoUuid(@Param("campoUuid") String campoUuid);

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.contribuyente.uuid = :contribuyenteUuid")
    List<ReservaPuntual> findByContribuyenteUuid(@Param("contribuyenteUuid") String contribuyenteUuid);

    // Combined filters with relationships
    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.campoDeportivo.uuid = :campoUuid AND rp.fechaUso = :fechaUso")
    List<ReservaPuntual> findByCampoDeportivoAndFechaUso(
            @Param("campoUuid") String campoUuid,
            @Param("fechaUso") LocalDate fechaUso
    );

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.campoDeportivo.uuid = :campoUuid AND rp.fechaUso BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaPuntual> findByCampoDeportivoAndFechaUsoBetween(
            @Param("campoUuid") String campoUuid,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.estadoReserva = :estadoReserva")
    List<ReservaPuntual> findByReservaEstadoReserva(@Param("estadoReserva") String estadoReserva);

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.estadoReserva IN :estadosReserva")
    List<ReservaPuntual> findByReservaEstadoReservaIn(@Param("estadosReserva") List<String> estadosReserva);

    // Complex business queries
    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.campoDeportivo.uuid = :campoUuid " +
            "AND rp.fechaUso = :fechaUso " +
            "AND rp.reserva.estadoReserva IN ('APROBADA', 'CONFIRMADA', 'ACTIVA') " +
            "ORDER BY rp.horaInicio ASC")
    List<ReservaPuntual> findReservasActivasPorCampoYFecha(
            @Param("campoUuid") String campoUuid,
            @Param("fechaUso") LocalDate fechaUso
    );

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.campoDeportivo.uuid = :campoUuid " +
            "AND rp.fechaUso BETWEEN :fechaInicio AND :fechaFin " +
            "AND rp.reserva.estadoReserva IN ('APROBADA', 'CONFIRMADA', 'ACTIVA')")
    List<ReservaPuntual> findReservasActivasPorCampoYRangoFechas(
            @Param("campoUuid") String campoUuid,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    // Conflict detection queries
    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.campoDeportivo.uuid = :campoUuid " +
            "AND rp.fechaUso = :fechaUso " +
            "AND ((rp.horaInicio BETWEEN :horaInicio AND :horaFin) OR (rp.horaFin BETWEEN :horaInicio AND :horaFin) " +
            "OR (rp.horaInicio <= :horaInicio AND rp.horaFin >= :horaFin)) " +
            "AND rp.reserva.estadoReserva IN ('APROBADA', 'CONFIRMADA', 'ACTIVA') " +
            "AND rp.uuid != :excludeReservaUuid")
    List<ReservaPuntual> findReservasConflictivas(
            @Param("campoUuid") String campoUuid,
            @Param("fechaUso") LocalDate fechaUso,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("excludeReservaUuid") String excludeReservaUuid
    );

    // Paginated queries
    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.campoDeportivo.uuid = :campoUuid")
    Page<ReservaPuntual> findByCampoDeportivoUuid(@Param("campoUuid") String campoUuid, Pageable pageable);

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.fechaUso BETWEEN :fechaInicio AND :fechaFin")
    Page<ReservaPuntual> findByFechaUsoBetween(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            Pageable pageable
    );

    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.reserva.estadoReserva = :estadoReserva")
    Page<ReservaPuntual> findByReservaEstadoReserva(@Param("estadoReserva") String estadoReserva, Pageable pageable);

    // Count queries
    @Query("SELECT COUNT(rp) FROM ReservaPuntual rp WHERE rp.reserva.campoDeportivo.uuid = :campoUuid AND rp.fechaUso = :fechaUso")
    Long countByCampoDeportivoAndFechaUso(
            @Param("campoUuid") String campoUuid,
            @Param("fechaUso") LocalDate fechaUso
    );

    @Query("SELECT COUNT(rp) FROM ReservaPuntual rp WHERE rp.reserva.campoDeportivo.uuid = :campoUuid AND rp.fechaUso BETWEEN :fechaInicio AND :fechaFin")
    Long countByCampoDeportivoAndFechaUsoBetween(
            @Param("campoUuid") String campoUuid,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    @Query("SELECT COUNT(rp) FROM ReservaPuntual rp WHERE rp.categoria = :categoria")
    Long countByCategoria(@Param("categoria") String categoria);

    @Query("SELECT COUNT(rp) FROM ReservaPuntual rp WHERE rp.superficie = :superficie")
    Long countBySuperficie(@Param("superficie") String superficie);

    // Analytics and reporting queries
    @Query("SELECT rp.categoria, COUNT(rp) FROM ReservaPuntual rp GROUP BY rp.categoria")
    List<Object[]> countByCategoriaGroup();

    @Query("SELECT rp.superficie, COUNT(rp) FROM ReservaPuntual rp GROUP BY rp.superficie")
    List<Object[]> countBySuperficieGroup();

    @Query("SELECT rp.fechaUso, COUNT(rp) FROM ReservaPuntual rp WHERE rp.fechaUso BETWEEN :fechaInicio AND :fechaFin GROUP BY rp.fechaUso ORDER BY rp.fechaUso")
    List<Object[]> countByFechaUsoGroup(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    @Query("SELECT rp.reserva.campoDeportivo.nombre, COUNT(rp) FROM ReservaPuntual rp GROUP BY rp.reserva.campoDeportivo.nombre, rp.reserva.campoDeportivo.uuid")
    List<Object[]> countByCampoDeportivoGroup();

    // Find reservations with observations
    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.observaciones IS NOT NULL AND rp.observaciones != ''")
    List<ReservaPuntual> findWithObservaciones();

    // Find by real usage date (for tracking actual usage vs reservation)
    @Query("SELECT rp FROM ReservaPuntual rp WHERE rp.fechaUsoReal IS NOT NULL AND rp.fechaUsoReal BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaPuntual> findWithUsoRealBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}