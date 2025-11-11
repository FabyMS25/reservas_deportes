package com.gamq.deportes.repository;

import com.gamq.deportes.model.Contribuyente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {

    @Query("SELECT c FROM Contribuyente c WHERE c.uuid = ?1")
    Optional<Contribuyente> findByUuid(String uuid);

    @Query("SELECT c FROM Contribuyente c WHERE c.email = ?1")
    Optional<Contribuyente> findByEmail(String correo);

    @Query("SELECT c FROM Contribuyente c WHERE c.userName= ?1 and c.password= ?2")
    Optional<Contribuyente> Login(String user, String Pass);
}
