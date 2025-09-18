package com.gamq.deportes.repository;

import com.gamq.deportes.model.Tablero;
import com.gamq.deportes.model.canchas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableroRepository extends JpaRepository<Tablero,Long> {

    Optional<Tablero> findByUuid(String uuid);

}
