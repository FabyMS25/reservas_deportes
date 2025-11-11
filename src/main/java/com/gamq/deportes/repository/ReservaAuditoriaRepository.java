package com.gamq.deportes.repository;

import com.gamq.deportes.model.ReservaAuditoria;
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
public interface ReservaAuditoriaRepository extends JpaRepository<ReservaAuditoria, Long> {

    // Basic UUID search
    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.uuid = ?1")
    Optional<ReservaAuditoria> findByUuid(String uuid);

    // Reservation-based queries
    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.reserva.uuid = :reservaUuid")
    List<ReservaAuditoria> findByReservaUuid(@Param("reservaUuid") String reservaUuid);

    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.reserva.idReserva = :idReserva")
    List<ReservaAuditoria> findByReservaId(@Param("idReserva") Long idReserva);

    // Action and status-based queries
    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.accion = :accion")
    List<ReservaAuditoria> findByAccion(@Param("accion") String accion);

    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.accion IN :acciones")
    List<ReservaAuditoria> findByAccionIn(@Param("acciones") List<String> acciones);

    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.estadoAnterior = :estadoAnterior")
    List<ReservaAuditoria> findByEstadoAnterior(@Param("estadoAnterior") String estadoAnterior);

    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.estadoNuevo = :estadoNuevo")
    List<ReservaAuditoria> findByEstadoNuevo(@Param("estadoNuevo") String estadoNuevo);

    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.estadoAnterior = :estadoAnterior AND ra.estadoNuevo = :estadoNuevo")
    List<ReservaAuditoria> findByEstadoAnteriorAndEstadoNuevo(
            @Param("estadoAnterior") String estadoAnterior,
            @Param("estadoNuevo") String estadoNuevo
    );

    // User-based queries
    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.usuarioUuid = :usuarioUuid")
    List<ReservaAuditoria> findByUsuarioUuid(@Param("usuarioUuid") String usuarioUuid);

    // Date range queries
    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.fechaAccion BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaAuditoria> findByFechaAccionBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaAuditoria> findByFechaCreacionBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Combined filters
    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.reserva.uuid = :reservaUuid AND ra.accion = :accion")
    List<ReservaAuditoria> findByReservaUuidAndAccion(
            @Param("reservaUuid") String reservaUuid,
            @Param("accion") String accion
    );

    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.usuarioUuid = :usuarioUuid AND ra.fechaAccion BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaAuditoria> findByUsuarioUuidAndFechaAccionBetween(
            @Param("usuarioUuid") String usuarioUuid,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.accion = :accion AND ra.fechaAccion BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaAuditoria> findByAccionAndFechaAccionBetween(
            @Param("accion") String accion,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Paginated queries
    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.reserva.uuid = :reservaUuid")
    Page<ReservaAuditoria> findByReservaUuid(@Param("reservaUuid") String reservaUuid, Pageable pageable);

    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.accion = :accion")
    Page<ReservaAuditoria> findByAccion(@Param("accion") String accion, Pageable pageable);

    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.fechaAccion BETWEEN :fechaInicio AND :fechaFin")
    Page<ReservaAuditoria> findByFechaAccionBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );

    // Count queries
    @Query("SELECT COUNT(ra) FROM ReservaAuditoria ra WHERE ra.reserva.uuid = :reservaUuid")
    Long countByReservaUuid(@Param("reservaUuid") String reservaUuid);

    @Query("SELECT COUNT(ra) FROM ReservaAuditoria ra WHERE ra.accion = :accion")
    Long countByAccion(@Param("accion") String accion);

    @Query("SELECT COUNT(ra) FROM ReservaAuditoria ra WHERE ra.usuarioUuid = :usuarioUuid")
    Long countByUsuarioUuid(@Param("usuarioUuid") String usuarioUuid);

    @Query("SELECT COUNT(ra) FROM ReservaAuditoria ra WHERE ra.fechaAccion BETWEEN :fechaInicio AND :fechaFin")
    Long countByFechaAccionBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Analytics and reporting - CORRECTED QUERIES
    @Query("SELECT ra.accion, COUNT(ra) FROM ReservaAuditoria ra GROUP BY ra.accion")
    List<Object[]> countByAccionGroup();

    @Query("SELECT ra.usuarioUuid, COUNT(ra) FROM ReservaAuditoria ra GROUP BY ra.usuarioUuid")
    List<Object[]> countByUsuarioUuidGroup();

    // CORRECTED: Fixed the date grouping query
    @Query("SELECT CAST(ra.fechaAccion AS date), COUNT(ra) FROM ReservaAuditoria ra WHERE ra.fechaAccion BETWEEN :fechaInicio AND :fechaFin GROUP BY CAST(ra.fechaAccion AS date) ORDER BY CAST(ra.fechaAccion AS date)")
    List<Object[]> countByFechaAccionGroup(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Alternative date grouping query for MySQL
    // @Query(value = "SELECT DATE(ra.fecha_accion), COUNT(ra) FROM reservas_auditorias ra WHERE ra.fecha_accion BETWEEN :fechaInicio AND :fechaFin GROUP BY DATE(ra.fecha_accion) ORDER BY DATE(ra.fecha_accion)", nativeQuery = true)
    // List<Object[]> countByFechaAccionGroupNative(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    // Find latest audit records
    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.reserva.uuid = :reservaUuid ORDER BY ra.fechaAccion DESC")
    List<ReservaAuditoria> findLatestByReservaUuid(@Param("reservaUuid") String reservaUuid, Pageable pageable);

    // Find by IP address
    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.direccionIp = :direccionIp")
    List<ReservaAuditoria> findByDireccionIp(@Param("direccionIp") String direccionIp);

    // Additional useful queries
    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.observaciones IS NOT NULL AND ra.observaciones != ''")
    List<ReservaAuditoria> findWithObservaciones();

    @Query("SELECT ra FROM ReservaAuditoria ra WHERE ra.datosCambio IS NOT NULL AND ra.datosCambio != ''")
    List<ReservaAuditoria> findWithDatosCambio();
}