package com.gamq.deportes.repository;

import com.gamq.deportes.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface reservasRepository extends JpaRepository<Reserva, Long> {

    @Query("SELECT c FROM Reserva c WHERE c.uuid = ?1")
    Optional<Reserva> findByUuid(String uuid);

    @Query("SELECT r FROM Reserva r WHERE r.fechaReserva BETWEEN :fechaIni AND :fechaFin")
    List<Reserva> findByFechaReserva(@Param("fechaIni") Date fechaIni, @Param("fechaFin") Date fechaFin);

    @Query("SELECT r FROM Reserva r WHERE r.campoDeportivo.uuid = ?1")
    List<Reserva> findReservasByCancha(String uuidCancha);

}
