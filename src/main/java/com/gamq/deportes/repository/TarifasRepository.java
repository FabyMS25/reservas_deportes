package com.gamq.deportes.repository;


import com.gamq.deportes.model.Tarifa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarifasRepository extends JpaRepository<Tarifa, Long> {
    @Query("SELECT c FROM Tarifa c WHERE c.uuid = ?1")
    Optional<Tarifa> findByUuid(String uuid);

    @Query("SELECT t FROM Tarifa t WHERE t.campoDeportivo.uuid = ?1")
    List<Tarifa> findTarifasByCanchaUuid(String canchaUuid);

}
