package com.gamq.deportes.repository;

import com.gamq.deportes.model.TarifaConcepto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaConceptoRepository extends JpaRepository<TarifaConcepto, Long> {

    // Find by UUID
    @Query("SELECT tc FROM TarifaConcepto tc WHERE tc.uuid = :uuid AND tc.estado = false")
    Optional<TarifaConcepto> findByUuid(@Param("uuid") String uuid);

    // Find all conceptos by tarifa UUID
    @Query("SELECT tc FROM TarifaConcepto tc WHERE tc.tarifa.uuid = :tarifaUuid AND tc.estado = false ORDER BY tc.concepto")
    List<TarifaConcepto> findByTarifaUuid(@Param("tarifaUuid") String tarifaUuid);
}