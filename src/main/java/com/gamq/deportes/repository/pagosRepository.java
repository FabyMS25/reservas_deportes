package com.gamq.deportes.repository;

import com.gamq.deportes.model.pagos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface pagosRepository extends JpaRepository<pagos,Long> {

    @Query("SELECT c FROM pagos c WHERE c.uuid = ?1")
    Optional<pagos> findByUuid(String uuid);

    @Query("SELECT pa FROM pagos pa WHERE pa.fechaPago BETWEEN :fechaIni AND :fechaFin")
    List<pagos> findByFechaPago(@Param("fechaIni") Date fechaIni, @Param("fechaFin") Date fechaFin);

}
