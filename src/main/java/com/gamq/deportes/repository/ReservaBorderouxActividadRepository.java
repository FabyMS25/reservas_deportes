package com.gamq.deportes.repository;

import com.gamq.deportes.model.ReservaBorderouxActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaBorderouxActividadRepository extends JpaRepository<ReservaBorderouxActividad, Long> {

    @Query("SELECT a FROM ReservaBorderouxActividad a WHERE a.uuid = ?1")
    Optional<ReservaBorderouxActividad> findByUuid(String uuid);

    @Query("SELECT a FROM ReservaBorderouxActividad a WHERE a.reserva.uuid = ?1 ORDER BY a.fechaActividad ASC, a.idReservaBorderouxActividad ASC")
    List<ReservaBorderouxActividad> findByReservaUuid(String reservaUuid);
}
