package com.gamq.licencias.repository;

import com.gamq.licencias.model.encargadoComplejo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface encargadoComplejoRepository extends JpaRepository<encargadoComplejo, Long> {

    @Query("SELECT c FROM encargadoComplejo c WHERE c.uuid = ?1")
    Optional<encargadoComplejo> findByUuid(String uuid);
    @Query("SELECT c FROM encargadoComplejo c WHERE c.userUuid = ?1")
    Optional<encargadoComplejo> findByUserUuid(String userUuid);

}
