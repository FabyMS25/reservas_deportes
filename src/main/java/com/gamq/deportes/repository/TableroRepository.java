package com.gamq.deportes.repository;

import com.gamq.deportes.model.Tablero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableroRepository extends JpaRepository<Tablero,Long> {

    Optional<Tablero> findByUuid(String uuid);

}
