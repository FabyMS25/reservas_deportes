package com.gamq.licencias.repository;

import com.gamq.licencias.model.encargadoCancha;
import com.gamq.licencias.model.pagos;
import com.gamq.licencias.model.reservas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface reservasRepository extends JpaRepository<reservas, Long> {

    @Query("SELECT c FROM reservas c WHERE c.uuid = ?1")
    Optional<reservas> findByUuid(String uuid);

    @Query("SELECT r FROM reservas r WHERE r.fechaReserva BETWEEN :fechaIni AND :fechaFin")
    List<reservas> findByFechaReserva(@Param("fechaIni") Date fechaIni, @Param("fechaFin") Date fechaFin);
}
