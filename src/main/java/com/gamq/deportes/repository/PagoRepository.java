package com.gamq.deportes.repository;

import com.gamq.deportes.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago,Long> {

    @Query("SELECT c FROM Pago c WHERE c.uuid = ?1")
    Optional<Pago> findByUuid(String uuid);

    @Query("SELECT pa FROM Pago pa WHERE pa.fechaPago BETWEEN :fechaIni AND :fechaFin")
    List<Pago> findByFechaPago(@Param("fechaIni") Date fechaIni, @Param("fechaFin") Date fechaFin);

}
