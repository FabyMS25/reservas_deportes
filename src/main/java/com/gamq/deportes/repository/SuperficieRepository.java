package com.gamq.deportes.repository;

import com.gamq.deportes.model.Superficie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface SuperficieRepository extends JpaRepository<Superficie, Long> {

    @Query("SELECT s FROM Superficie s WHERE s.uuid = ?1")
    Optional<Superficie> findByUuid(@Param("uuid") String uuid);

    @Query("SELECT s FROM Superficie s WHERE s.estado = false")
    List<Superficie> findAllActivos();

    @Query("SELECT s FROM Superficie s WHERE s.descripcion = :descripcion AND s.estado = false")
    Optional<Superficie> findByDescripcion(@Param("descripcion") String descripcion);

    boolean existsByUuidAndEstadoFalse(String uuid);
}