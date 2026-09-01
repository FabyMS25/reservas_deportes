package com.gamq.deportes.repository;

import com.gamq.deportes.model.Contribuyente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {

    @Query("SELECT c FROM Contribuyente c WHERE c.uuid = ?1")
    Optional<Contribuyente> findByUuid(String uuid);

    @Query("SELECT c FROM Contribuyente c WHERE c.email = ?1")
    Optional<Contribuyente> findByEmail(String correo);

    @Query("SELECT c FROM Contribuyente c WHERE c.userName = ?1")
    Optional<Contribuyente> findByUserName(String userName);

    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUserNameIgnoreCase(String userName);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM contribuyentes WHERE numero_documento = :value)", nativeQuery = true)
    boolean existsAnyByNumeroDocumento(@Param("value") String value);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM contribuyentes WHERE LOWER(email) = LOWER(:value))", nativeQuery = true)
    boolean existsAnyByEmail(@Param("value") String value);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM contribuyentes WHERE LOWER(user_name) = LOWER(:value))", nativeQuery = true)
    boolean existsAnyByUserName(@Param("value") String value);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM contribuyentes WHERE codigo_contribuyente = :value)", nativeQuery = true)
    boolean existsAnyByCodigoContribuyente(@Param("value") String value);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM contribuyentes WHERE nro_nit = :value)", nativeQuery = true)
    boolean existsAnyByNroNit(@Param("value") String value);
}
