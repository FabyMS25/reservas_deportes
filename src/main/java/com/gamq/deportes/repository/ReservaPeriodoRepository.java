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
    Optional<ReservaPeriodo> findByReservaUuid(String reservaUuid);

    @Query("SELECT rp FROM ReservaPeriodo rp WHERE rp.mes = ?1")
    List<ReservaPeriodo> findByPeriodo(String periodo);

    @Query("SELECT rp FROM ReservaPeriodo rp WHERE rp.reserva.contribuyente.uuid = ?1")
    List<ReservaPeriodo> findByContribuyenteUuid(String contribuyenteUuid);
    // In your ReservaRepository
    @Query("SELECT r FROM Reserva r WHERE r.numeroReserva = :numeroReserva AND r.estado = false")
    Optional<Reserva> findByNumeroReserva(@Param("numeroReserva") String numeroReserva);

    @Query("SELECT rp FROM ReservaPeriodo rp WHERE rp.reserva.organizacion.uuid = ?1")
    List<ReservaPeriodo> findByOrganizacionUuid(String organizacionUuid);
}