package com.gamq.deportes.repository;

import com.gamq.deportes.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("SELECT c FROM Reserva c WHERE c.uuid = ?1")
    Optional<Reserva> findByUuid(String uuid);

    @Query("SELECT r FROM Reserva r WHERE r.campoDeportivo.uuid = ?1")
    List<Reserva> findReservasByCancha(String uuidCancha);

    @Query("SELECT r FROM Reserva r WHERE r.fechaReserva BETWEEN :fechaIni AND :fechaFin")
    List<Reserva> findByFechaReservaBetween(
            @Param("fechaIni") LocalDateTime fechaIni,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT r FROM Reserva r WHERE r.fechaCreacion BETWEEN :fechaIni AND :fechaFin")
    List<Reserva> findByFechaCreacionBetween(
            @Param("fechaIni") LocalDateTime fechaIni,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT r FROM Reserva r WHERE r.fechaAprobacion BETWEEN :fechaIni AND :fechaFin")
    List<Reserva> findByFechaAprobacionBetween(
            @Param("fechaIni") LocalDateTime fechaIni,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT r FROM Reserva r WHERE r.fechaRechazo BETWEEN :fechaIni AND :fechaFin")
    List<Reserva> findByFechaRechazoBetween(
            @Param("fechaIni") LocalDateTime fechaIni,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Field-specific searches
    @Query("SELECT r FROM Reserva r WHERE r.campoDeportivo.uuid = :uuidCancha")
    List<Reserva> findByCampoDeportivoUuid(@Param("uuidCancha") String uuidCancha);

    @Query("SELECT r FROM Reserva r WHERE r.contribuyente.uuid = :uuidContribuyente")
    List<Reserva> findByContribuyenteUuid(@Param("uuidContribuyente") String uuidContribuyente);

    @Query("SELECT r FROM Reserva r WHERE r.organizacion.uuid = :uuidOrganizacion")
    List<Reserva> findByOrganizacionUuid(@Param("uuidOrganizacion") String uuidOrganizacion);

    @Query("SELECT r FROM Reserva r WHERE r.tarifa.uuid = :uuidTarifa")
    List<Reserva> findByTarifaUuid(@Param("uuidTarifa") String uuidTarifa);

    @Query("SELECT r FROM Reserva r WHERE r.numeroReserva = :numeroReserva")
    Optional<Reserva> findByNumeroReserva(@Param("numeroReserva") String numeroReserva);

    @Query("SELECT r FROM Reserva r WHERE r.numeroTasa = :numeroTasa")
    Optional<Reserva> findByNumeroTasa(@Param("numeroTasa") String numeroTasa);

    // Status-based queries
    @Query("SELECT r FROM Reserva r WHERE r.estadoReserva = :estadoReserva")
    List<Reserva> findByEstadoReserva(@Param("estadoReserva") String estadoReserva);

    @Query("SELECT r FROM Reserva r WHERE r.estadoReserva IN :estados")
    List<Reserva> findByEstadoReservaIn(@Param("estados") List<String> estados);

    @Query("SELECT r FROM Reserva r WHERE r.estadoValidacion = :estadoValidacion")
    List<Reserva> findByEstadoValidacion(@Param("estadoValidacion") String estadoValidacion);

    @Query("SELECT r FROM Reserva r WHERE r.tipoReserva = :tipoReserva")
    List<Reserva> findByTipoReserva(@Param("tipoReserva") String tipoReserva);

    // Combined filters
    @Query("SELECT r FROM Reserva r WHERE r.campoDeportivo.uuid = :uuidCancha AND r.estadoReserva = :estadoReserva")
    List<Reserva> findByCampoDeportivoAndEstadoReserva(
            @Param("uuidCancha") String uuidCancha,
            @Param("estadoReserva") String estadoReserva
    );

    @Query("SELECT r FROM Reserva r WHERE r.contribuyente.uuid = :uuidContribuyente AND r.estadoReserva = :estadoReserva")
    List<Reserva> findByContribuyenteAndEstadoReserva(
            @Param("uuidContribuyente") String uuidContribuyente,
            @Param("estadoReserva") String estadoReserva
    );

    @Query("SELECT r FROM Reserva r WHERE r.fechaReserva BETWEEN :fechaIni AND :fechaFin AND r.estadoReserva = :estadoReserva")
    List<Reserva> findByFechaReservaAndEstadoReserva(
            @Param("fechaIni") LocalDateTime fechaIni,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("estadoReserva") String estadoReserva
    );

    // Approval related queries
    @Query("SELECT r FROM Reserva r WHERE r.aprobadoPor = :aprobadoPor")
    List<Reserva> findByAprobadoPor(@Param("aprobadoPor") String aprobadoPor);

    @Query("SELECT r FROM Reserva r WHERE r.fechaAprobacion IS NOT NULL")
    List<Reserva> findAprobadas();

    @Query("SELECT r FROM Reserva r WHERE r.fechaRechazo IS NOT NULL")
    List<Reserva> findRechazadas();

    @Query("SELECT r FROM Reserva r WHERE r.fechaAprobacion IS NOT NULL AND r.fechaAprobacion BETWEEN :fechaIni AND :fechaFin")
    List<Reserva> findAprobadasEntreFechas(
            @Param("fechaIni") LocalDateTime fechaIni,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT r FROM Reserva r WHERE r.fechaRechazo IS NOT NULL AND r.fechaRechazo BETWEEN :fechaIni AND :fechaFin")
    List<Reserva> findRechazadasEntreFechas(
            @Param("fechaIni") LocalDateTime fechaIni,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Paginated queries
    @Query("SELECT r FROM Reserva r WHERE r.estadoReserva = :estadoReserva")
    Page<Reserva> findByEstadoReserva(@Param("estadoReserva") String estadoReserva, Pageable pageable);

    @Query("SELECT r FROM Reserva r WHERE r.campoDeportivo.uuid = :uuidCancha")
    Page<Reserva> findByCampoDeportivoUuid(@Param("uuidCancha") String uuidCancha, Pageable pageable);

    @Query("SELECT r FROM Reserva r WHERE r.contribuyente.uuid = :uuidContribuyente")
    Page<Reserva> findByContribuyenteUuid(@Param("uuidContribuyente") String uuidContribuyente, Pageable pageable);

    @Query("SELECT r FROM Reserva r WHERE r.fechaReserva BETWEEN :fechaIni AND :fechaFin")
    Page<Reserva> findByFechaReservaBetween(
            @Param("fechaIni") LocalDateTime fechaIni,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );

    // Count queries
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.campoDeportivo.uuid = :uuidCancha AND r.estadoReserva = :estadoReserva")
    Long countByCampoDeportivoAndEstadoReserva(
            @Param("uuidCancha") String uuidCancha,
            @Param("estadoReserva") String estadoReserva
    );

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.contribuyente.uuid = :uuidContribuyente AND r.estadoReserva = :estadoReserva")
    Long countByContribuyenteAndEstadoReserva(
            @Param("uuidContribuyente") String uuidContribuyente,
            @Param("estadoReserva") String estadoReserva
    );

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.estadoReserva = :estadoReserva")
    Long countByEstadoReserva(@Param("estadoReserva") String estadoReserva);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.fechaReserva BETWEEN :fechaIni AND :fechaFin")
    Long countByFechaReservaBetween(
            @Param("fechaIni") LocalDateTime fechaIni,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Complex business queries
    @Query("SELECT r FROM Reserva r WHERE r.campoDeportivo.uuid = :uuidCancha " +
            "AND r.fechaReserva BETWEEN :fechaIni AND :fechaFin " +
            "AND r.estadoReserva IN ('APROBADA', 'CONFIRMADA', 'ACTIVA')")
    List<Reserva> findReservasActivasPorCampoYFecha(
            @Param("uuidCancha") String uuidCancha,
            @Param("fechaIni") LocalDateTime fechaIni,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT r FROM Reserva r WHERE r.contribuyente.uuid = :uuidContribuyente " +
            "AND r.estadoReserva IN ('PENDIENTE', 'APROBADA', 'CONFIRMADA') " +
            "ORDER BY r.fechaReserva ASC")
    List<Reserva> findReservasActivasPorContribuyente(@Param("uuidContribuyente") String uuidContribuyente);

    @Query("SELECT r FROM Reserva r WHERE r.estadoReserva = 'PENDIENTE' " +
            "AND r.fechaCreacion < :fechaLimite " +
            "ORDER BY r.fechaCreacion ASC")
    List<Reserva> findReservasPendientesExpiradas(@Param("fechaLimite") LocalDateTime fechaLimite);

    // For reporting and analytics
    @Query("SELECT r.estadoReserva, COUNT(r) FROM Reserva r GROUP BY r.estadoReserva")
    List<Object[]> countReservasPorEstadoReserva();

    @Query("SELECT r.tipoReserva, COUNT(r) FROM Reserva r GROUP BY r.tipoReserva")
    List<Object[]> countReservasPorTipoReserva();

    @Query("SELECT cd.nombre, COUNT(r) FROM Reserva r JOIN r.campoDeportivo cd GROUP BY cd.nombre, cd.uuid")
    List<Object[]> countReservasPorCampoDeportivo();

    @Query("SELECT r.clasificadorRuatId, COUNT(r) FROM Reserva r GROUP BY r.clasificadorRuatId")
    List<Object[]> countReservasPorClasificadorRuat();

    @Query(value = "SELECT * FROM reservas r WHERE r.id_campo = :idCampo " +
            "AND DATE(r.fecha_reserva) = DATE(:fecha) " +
            "AND r.estado_reserva IN ('APROBADA', 'CONFIRMADA')",
            nativeQuery = true)
    List<Reserva> findReservasActivasPorCampoIdYFecha(
            @Param("idCampo") Long idCampo,
            @Param("fecha") LocalDateTime fecha
    );

    @Query("SELECT r FROM Reserva r WHERE r.campoDeportivo.uuid = :uuidCancha " +
            "AND r.fechaReserva BETWEEN :fechaIni AND :fechaFin " +
            "AND r.estadoReserva IN ('APROBADA', 'CONFIRMADA', 'ACTIVA') " +
            "AND r.uuid != :excludeReservaUuid")
    List<Reserva> findReservasConflictivas(
            @Param("uuidCancha") String uuidCancha,
            @Param("fechaIni") LocalDateTime fechaIni,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("excludeReservaUuid") String excludeReservaUuid
    );

    // Find by reservation type details
    @Query("SELECT r FROM Reserva r WHERE r.reservaPuntual IS NOT NULL")
    List<Reserva> findReservasPuntuales();

    @Query("SELECT r FROM Reserva r WHERE r.reservaRecurrente IS NOT NULL")
    List<Reserva> findReservasRecurrentes();

    @Query("SELECT r FROM Reserva r WHERE r.montoTotal BETWEEN :montoMin AND :montoMax")
    List<Reserva> findByMontoTotalBetween(
            @Param("montoMin") BigDecimal montoMin,
            @Param("montoMax") BigDecimal montoMax
    );

    @Query("SELECT r FROM Reserva r WHERE r.estadoReserva = 'PENDIENTE' AND r.estadoValidacion = 'PENDIENTE'")
    List<Reserva> findPendientesDeAprobacion();

    // Add these methods to your ReservaRepository
    @Query("SELECT r.numeroReserva FROM Reserva r WHERE r.numeroReserva LIKE :yearPattern ORDER BY r.numeroReserva DESC")
    List<String> findLatestReservationNumbersByYear(@Param("yearPattern") String yearPattern);

    @Query("SELECT MAX(CAST(SUBSTRING(r.numeroReserva, 1, 4) AS long)) FROM Reserva r WHERE r.numeroReserva LIKE :yearPattern")
    Optional<Long> findMaxReservationNumberByYear(@Param("yearPattern") String yearPattern);

    @Query("SELECT r FROM Reserva r WHERE r.tarifa.idTarifa = :idTarifa AND r.estado = false AND r.estadoValidacion != 'RECHAZADA'")
    List<Reserva> findActiveReservasByTarifa(@Param("idTarifa") Long idTarifa);
}