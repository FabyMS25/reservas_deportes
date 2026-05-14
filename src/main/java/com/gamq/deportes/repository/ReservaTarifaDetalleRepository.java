package com.gamq.deportes.repository;

import com.gamq.deportes.model.ReservaTarifaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaTarifaDetalleRepository extends JpaRepository<ReservaTarifaDetalle, Long> {

    @Query("SELECT d FROM ReservaTarifaDetalle d WHERE d.uuid = ?1")
    Optional<ReservaTarifaDetalle> findByUuid(String uuid);

    @Query("SELECT d FROM ReservaTarifaDetalle d WHERE d.reserva.uuid = ?1 ORDER BY d.idReservaTarifaDetalle ASC")
    List<ReservaTarifaDetalle> findByReservaUuid(String reservaUuid);
}
