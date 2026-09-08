package com.gamq.deportes.repository;

import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.ReservaPeriodo;
import com.gamq.deportes.model.ReservaRecurrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaPeriodoRepository extends JpaRepository<ReservaPeriodo, Long>  ,
        JpaSpecificationExecutor<ReservaPeriodo> {

    @Query("SELECT rp FROM ReservaPeriodo rp WHERE rp.uuid = ?1")
    Optional<ReservaPeriodo> findByUuid(String uuid);

    @Query("SELECT rp FROM ReservaPeriodo rp WHERE rp.reserva.uuid = ?1")
    List<ReservaPeriodo> findByReservaUuid(String reservaUuid);

    @Query("SELECT rp FROM ReservaPeriodo rp WHERE rp.año = :anio AND rp.mes = :mes")
    List<ReservaPeriodo> findByPeriodo(@Param("anio") Integer anio, @Param("mes") Integer mes);

    @Query("SELECT DISTINCT rp FROM ReservaPeriodo rp " +
            "LEFT JOIN rp.estudianteEscuela e " +
            "LEFT JOIN e.contribuyenteDto tutor " +
            "WHERE rp.reserva.contribuyente.uuid = :contribuyenteUuid " +
            "OR tutor.uuid = :contribuyenteUuid " +
            "OR rp.reserva.organizacion.representante.uuid = :contribuyenteUuid " +
            "OR EXISTS (SELECT p.id FROM OrganizacionRepresentante p " +
            "WHERE p.organizacion = rp.reserva.organizacion " +
            "AND p.contribuyente.uuid = :contribuyenteUuid)")
    List<ReservaPeriodo> findByContribuyenteUuid(@Param("contribuyenteUuid") String contribuyenteUuid);

    @Query("SELECT rp FROM ReservaPeriodo rp " +
            "JOIN rp.estudianteEscuela e " +
            "JOIN rp.reserva r " +
            "WHERE e.uuid = :estudianteUuid " +
            "AND UPPER(COALESCE(r.estadoReserva, '')) NOT IN " +
            "('ANULADO', 'ANULADA', 'CANCELADA', 'EXPIRADA') " +
            "ORDER BY rp.año DESC, rp.mes DESC, rp.fechaCreacion DESC")
    List<ReservaPeriodo> findByEstudianteUuid(@Param("estudianteUuid") String estudianteUuid);
    @Query("SELECT r FROM Reserva r WHERE r.numeroReserva = :numeroReserva AND r.estado = false")
    Optional<Reserva> findByNumeroReserva(@Param("numeroReserva") String numeroReserva);

    @Query("SELECT rp FROM ReservaPeriodo rp WHERE rp.reserva.organizacion.uuid = ?1")
    List<ReservaPeriodo> findByOrganizacionUuid(String organizacionUuid);
}
