package com.gamq.deportes.repository;

import com.gamq.deportes.model.ReservaBorderouxTalonario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaBorderouxTalonarioRepository extends JpaRepository<ReservaBorderouxTalonario, Long> {

    @Query("SELECT t FROM ReservaBorderouxTalonario t WHERE t.uuid = ?1")
    Optional<ReservaBorderouxTalonario> findByUuid(String uuid);

    @Query("SELECT t FROM ReservaBorderouxTalonario t WHERE t.actividad.uuid = ?1 ORDER BY t.numeroInicial ASC")
    List<ReservaBorderouxTalonario> findByActividadUuid(String actividadUuid);
}
