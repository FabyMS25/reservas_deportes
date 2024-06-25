package com.gamq.licencias.repository;
import com.gamq.licencias.model.encargadoCancha;
import com.gamq.licencias.model.horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface horarioRepository extends JpaRepository<horario, Long> {

    @Query("SELECT c FROM horario c WHERE c.uuid = ?1")
    Optional<horario> findByUuid(String uuid);
}
