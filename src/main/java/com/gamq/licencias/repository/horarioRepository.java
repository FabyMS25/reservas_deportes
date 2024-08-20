package com.gamq.licencias.repository;
import com.gamq.licencias.model.Tarifas;
import com.gamq.licencias.model.horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface horarioRepository extends JpaRepository<horario, Long> {

    @Query("SELECT c FROM horario c WHERE c.uuid = ?1")
    Optional<horario> findByUuid(String uuid);

    @Query("SELECT h FROM horario h WHERE h.cancha.uuid = ?1")
    List<horario> findHorariosByCanchaUuid(String canchaUuid);
}
