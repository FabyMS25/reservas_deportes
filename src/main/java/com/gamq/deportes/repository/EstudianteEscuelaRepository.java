package com.gamq.deportes.repository;

import com.gamq.deportes.model.EstudianteEscuela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteEscuelaRepository extends JpaRepository<EstudianteEscuela, Long> {

    @Query("SELECT e FROM EstudianteEscuela e WHERE e.uuid = ?1")
    Optional<EstudianteEscuela> findByUuid(String uuid);

    @Query("SELECT e FROM EstudianteEscuela e WHERE e.matricula = ?1")
    Optional<EstudianteEscuela> findByMatricula(String matricula);

    @Query("SELECT e FROM EstudianteEscuela e WHERE e.numeroDocumento = ?1")
    Optional<EstudianteEscuela> findByNumeroDocumento(String numeroDocumento);

    @Query("SELECT e FROM EstudianteEscuela e WHERE e.contribuyenteDto.uuid = ?1")
    List<EstudianteEscuela> findByContribuyenteUuid(String uuidContribuyente);

//    @Query("SELECT e FROM EstudianteEscuela e WHERE e.organizacionDto.uuid = ?1")
//    List<EstudianteEscuela> findByOrganizacionUuid(String uuidOrganizacion);

    @Query("SELECT e FROM EstudianteEscuela e WHERE e.activo = true")
    List<EstudianteEscuela> findByActivoTrue();

//    @Query("SELECT e FROM EstudianteEscuela e WHERE e.activo = true AND e.organizacionDto.uuid = ?1")
//    List<EstudianteEscuela> findByActivoTrueAndOrganizacionUuid(String uuidOrganizacion);

    @Query("SELECT e FROM EstudianteEscuela e WHERE e.contribuyenteDto.uuid = ?1 AND e.activo = true")
    List<EstudianteEscuela> findByContribuyenteUuidAndActivoTrue(String uuidContribuyente);

//    @Query("SELECT COUNT(e) FROM EstudianteEscuela e WHERE e.organizacionDto.uuid = ?1 AND e.activo = true")
//    Long countByOrganizacionUuidAndActivoTrue(String uuidOrganizacion);

    @Query("SELECT e FROM EstudianteEscuela e WHERE e.email = ?1")
    Optional<EstudianteEscuela> findByEmail(String email);
}