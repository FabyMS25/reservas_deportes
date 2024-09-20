package com.gamq.deportes.repository;

import com.gamq.deportes.model.reservas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface reservasRepository extends JpaRepository<reservas, Long> {

    @Query("SELECT c FROM reservas c WHERE c.uuid = ?1")
    Optional<reservas> findByUuid(String uuid);

    @Query("SELECT r FROM reservas r WHERE r.fechaReserva BETWEEN :fechaIni AND :fechaFin")
    List<reservas> findByFechaReserva(@Param("fechaIni") Date fechaIni, @Param("fechaFin") Date fechaFin);

    @Query("SELECT r FROM reservas r WHERE r.cancha.uuid = ?1")
    List<reservas> findReservasByCancha(String uuidCancha);

}
