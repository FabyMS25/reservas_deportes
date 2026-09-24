package com.gamq.deportes.repository;

import com.gamq.deportes.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
public interface ReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {
    @org.springframework.data.jpa.repository.Lock(javax.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.QueryHints(@javax.persistence.QueryHint(name = "javax.persistence.lock.timeout", value = "0"))
    @Query("SELECT r FROM Reserva r WHERE r.uuid = :uuid")
    Optional<Reserva> findByUuidForRevision(@Param("uuid") String uuid);
    @Query("SELECT DISTINCT r FROM Reserva r LEFT JOIN r.contribuyente c LEFT JOIN r.organizacion o LEFT JOIN r.campoDeportivo cd LEFT JOIN r.reservasPeriodoList rp  WHERE (:typeReserva IS NULL OR r.tipoReserva = :typeReserva) AND (:estadoReserva IS NULL OR r.estadoReserva = :estadoReserva) AND (:estadoValidacion IS NULL OR r.estadoValidacion = :estadoValidacion) AND (:esReservaGratuita IS NULL OR r.esReservaGratuita = :esReservaGratuita) AND (:organization IS NULL OR o.uuid = :organization) AND (:mesFiltro IS NULL OR (r.tipoReserva = 'periodo' AND rp.mes = :mesFiltro)) AND (:anioFiltro IS NULL OR (r.tipoReserva = 'periodo' AND rp.año = :anioFiltro )) AND (:searchTerm IS NULL OR LOWER(CONCAT( COALESCE(c.nombre,''),' ', COALESCE(c.primerApellido,''),' ', COALESCE(c.segundoApellido,''))) LIKE LOWER(CONCAT('%',:searchTerm,'%')) OR LOWER(o.nombre) LIKE LOWER(CONCAT('%',:searchTerm,'%')) OR LOWER(cd.nombre) LIKE LOWER(CONCAT('%',:searchTerm,'%')) OR LOWER(r.numeroReserva) LIKE LOWER(CONCAT('%',:searchTerm,'%')) OR LOWER(r.numeroTasa) LIKE LOWER(CONCAT('%',:searchTerm,'%')) OR LOWER(r.motivoRechazo) LIKE LOWER(CONCAT('%',:searchTerm,'%')) OR LOWER(r.motivoGratuidad) LIKE LOWER(CONCAT('%',:searchTerm,'%')))")
    Page<Reserva> buscarReservas(
            @Param("searchTerm") String searchTerm,
            @Param("typeReserva") String typeReserva,
            @Param("estadoReserva") String estadoReserva,
            @Param("estadoValidacion") String estadoValidacion,
            @Param("esReservaGratuita") Boolean esReservaGratuita,
            @Param("organization") String organization,
            @Param("mesFiltro") Integer mesFiltro,
            @Param("anioFiltro") Integer anioFiltro,
            Pageable pageable
    );

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
            "AND r.estadoReserva IN ('PENDIENTE', 'PAGADA', 'EN_CURSO')")
    List<Reserva> findReservasActivasPorCampoYFecha(
            @Param("uuidCancha") String uuidCancha,
            @Param("fechaIni") LocalDateTime fechaIni,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT r FROM Reserva r WHERE r.contribuyente.uuid = :uuidContribuyente " +
            "AND r.estadoReserva IN ('PENDIENTE', 'PAGADA', 'EN_CURSO') " +
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
            "AND r.estado = false " +
            "AND r.estado_reserva IN ('PENDIENTE', 'PAGADA', 'EN_CURSO')",
            nativeQuery = true)
    List<Reserva> findReservasActivasPorCampoIdYFecha(
            @Param("idCampo") Long idCampo,
            @Param("fecha") LocalDateTime fecha
    );

    @Query("SELECT r FROM Reserva r WHERE r.campoDeportivo.uuid = :uuidCancha " +
            "AND r.fechaReserva BETWEEN :fechaIni AND :fechaFin " +
            "AND r.estadoReserva IN ('PENDIENTE', 'PAGADA', 'EN_CURSO') " +
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

    @Query("SELECT r FROM Reserva r WHERE r.tarifa.idTarifa = :idTarifa AND r.estado = false AND r.estadoValidacion != 'RECHAZADA'")
    List<Reserva> findActiveReservasByTarifa(@Param("idTarifa") Long idTarifa);


    @Query("SELECT MAX(CAST(SUBSTRING(r.numeroReserva, 1, 4) AS long)) " +
            "FROM Reserva r " +
            "WHERE r.numeroReserva LIKE :yearPattern")
    Optional<Long> findMaxReservationNumberByYear(@Param("yearPattern") String yearPattern);

    @Query("SELECT r FROM Reserva r " +
            "WHERE r.numeroReserva LIKE CONCAT(:prefix, '-%') " +
            "AND SUBSTRING(r.numeroReserva, LENGTH(r.numeroReserva) - 1, 2) = :year " +
            "ORDER BY CAST(SUBSTRING(r.numeroReserva, 3, 4) AS long) DESC")
    List<Reserva> findByPrefixAndYear(@Param("prefix") String prefix, @Param("year") String year);

    @Query("SELECT MAX(CAST(SUBSTRING(r.numeroReserva, 3, 4) AS long)) " +
            "FROM Reserva r " +
            "WHERE r.numeroReserva LIKE :prefixYearPattern")
    Optional<Long> findMaxReservationNumberByPrefixAndYear(@Param("prefixYearPattern") String prefixYearPattern);

    // Alternative query that might be more reliable
    @Query("SELECT MAX(CAST(SUBSTRING(r.numeroReserva, LOCATE('-', r.numeroReserva) + 1, 4) AS long)) " +
            "FROM Reserva r " +
            "WHERE r.numeroReserva LIKE CONCAT(:prefix, '-%') " +
            "AND SUBSTRING(r.numeroReserva, LENGTH(r.numeroReserva) - 1, 2) = :year")
    Optional<Long> findMaxNumberByPrefixAndYearAlternative(
            @Param("prefix") String prefix,
            @Param("year") String year);
}
