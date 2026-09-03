package com.gamq.deportes.repository;

import com.gamq.deportes.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    Optional<Estudiante> findByUuid(String uuid);
    Optional<Estudiante> findByNumeroDocumento(String numeroDocumento);
}
