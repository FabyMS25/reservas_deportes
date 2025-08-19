package com.gamq.deportes.repository;

import com.gamq.deportes.model.contribuyentes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface contribuyentesRepository extends JpaRepository<contribuyentes, Long> {

    @Query("SELECT c FROM contribuyentes c WHERE c.uuid = ?1")
    Optional<contribuyentes> findByUuid(String uuid);

    @Query("SELECT c FROM contribuyentes c WHERE c.correo = ?1")
    Optional<contribuyentes> findByEmail(String correo);

    @Query("SELECT c FROM contribuyentes c WHERE c.userName= ?1 and c.password= ?2")
    Optional<contribuyentes> Login(String user, String Pass);
}
