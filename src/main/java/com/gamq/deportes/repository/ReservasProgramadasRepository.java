package com.gamq.deportes.repository;


import com.gamq.deportes.model.ReservasProgramadas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservasProgramadasRepository extends JpaRepository<ReservasProgramadas,Long> {

    @Query("SELECT c FROM ReservasProgramadas c WHERE c.uuid = ?1")
    Optional<ReservasProgramadas> findByUuid(String uuid);

}
