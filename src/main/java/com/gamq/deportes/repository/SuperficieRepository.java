package com.gamq.deportes.repository;

import com.gamq.deportes.model.Superficie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface SuperficieRepository extends JpaRepository<Superficie, Long> {

    @Query("SELECT c FROM Superficie c WHERE c.uuid = ?1")
    Optional<Superficie> findByUuid(String uuid);
}
