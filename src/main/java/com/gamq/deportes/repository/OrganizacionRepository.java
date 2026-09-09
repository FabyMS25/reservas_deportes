package com.gamq.deportes.repository;

import com.gamq.deportes.model.Organizacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizacionRepository extends JpaRepository<Organizacion, Long> {

    @org.springframework.data.jpa.repository.Lock(javax.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Organizacion o WHERE o.uuid = ?1")
    Optional<Organizacion> findByUuidForUpdate(String uuid);

    // Basic UUID search
    @Query("SELECT o FROM Organizacion o WHERE o.uuid = ?1")
    Optional<Organizacion> findByUuid(String uuid);

    // Field-specific searches
    @Query("SELECT o FROM Organizacion o WHERE o.nombre LIKE %:nombre%")
    List<Organizacion> findByNombreContaining(@Param("nombre") String nombre);

    @Query("SELECT o FROM Organizacion o WHERE o.nroNit = :nroNit")
    Optional<Organizacion> findByNroNit(@Param("nroNit") String nroNit);

    @Query("SELECT o FROM Organizacion o WHERE o.email = :email")
    Optional<Organizacion> findByEmail(@Param("email") String email);

    @Query("SELECT o FROM Organizacion o WHERE o.telefono = :telefono")
    Optional<Organizacion> findByTelefono(@Param("telefono") String telefono);

    // Status-based queries
    @Query("SELECT o FROM Organizacion o WHERE o.activo = :activo")
    List<Organizacion> findByActivo(@Param("activo") Boolean activo);

    @Query("SELECT o FROM Organizacion o WHERE o.tipoOrganizacion = :tipoOrganizacion")
    List<Organizacion> findByTipoOrganizacion(@Param("tipoOrganizacion") String tipoOrganizacion);

    @Query("SELECT o FROM Organizacion o WHERE o.tipoOrganizacion = :tipoOrganizacion AND o.activo = :activo")
    List<Organizacion> findByTipoOrganizacionAndActivo(
            @Param("tipoOrganizacion") String tipoOrganizacion,
            @Param("activo") Boolean activo
    );

    // Relationship-based queries
    @Query("SELECT DISTINCT o FROM Organizacion o WHERE EXISTS (SELECT p.id FROM OrganizacionRepresentante p WHERE p.organizacion = o AND p.contribuyente.uuid = :representanteUuid)")
    List<Organizacion> findByRepresentanteUuid(@Param("representanteUuid") String representanteUuid);

    @Query("SELECT DISTINCT o FROM Organizacion o WHERE EXISTS (SELECT p.id FROM OrganizacionRepresentante p WHERE p.organizacion = o AND p.contribuyente.idContribuyente = :idRepresentante)")
    List<Organizacion> findByRepresentanteId(@Param("idRepresentante") Long idRepresentante);

    // Combined filters
    @Query("SELECT o FROM Organizacion o WHERE o.nombre LIKE %:nombre% AND o.activo = :activo")
    List<Organizacion> findByNombreContainingAndActivo(
            @Param("nombre") String nombre,
            @Param("activo") Boolean activo
    );

    @Query("SELECT o FROM Organizacion o WHERE o.tipoOrganizacion = :tipoOrganizacion AND o.nombre LIKE %:nombre%")
    List<Organizacion> findByTipoOrganizacionAndNombreContaining(
            @Param("tipoOrganizacion") String tipoOrganizacion,
            @Param("nombre") String nombre
    );

    // Paginated queries
    @Query("SELECT o FROM Organizacion o WHERE o.activo = :activo")
    Page<Organizacion> findByActivo(@Param("activo") Boolean activo, Pageable pageable);

    @Query("SELECT o FROM Organizacion o WHERE o.tipoOrganizacion = :tipoOrganizacion")
    Page<Organizacion> findByTipoOrganizacion(@Param("tipoOrganizacion") String tipoOrganizacion, Pageable pageable);

    @Query("SELECT o FROM Organizacion o WHERE o.nombre LIKE %:nombre%")
    Page<Organizacion> findByNombreContaining(@Param("nombre") String nombre, Pageable pageable);

    // Count queries
    @Query("SELECT COUNT(o) FROM Organizacion o WHERE o.activo = :activo")
    Long countByActivo(@Param("activo") Boolean activo);

    @Query("SELECT COUNT(o) FROM Organizacion o WHERE o.tipoOrganizacion = :tipoOrganizacion")
    Long countByTipoOrganizacion(@Param("tipoOrganizacion") String tipoOrganizacion);

    @Query("SELECT COUNT(DISTINCT o) FROM Organizacion o WHERE EXISTS (SELECT p.id FROM OrganizacionRepresentante p WHERE p.organizacion = o AND p.contribuyente.uuid = :representanteUuid)")
    Long countByRepresentanteUuid(@Param("representanteUuid") String representanteUuid);

    // Analytics and reporting
    @Query("SELECT o.tipoOrganizacion, COUNT(o) FROM Organizacion o GROUP BY o.tipoOrganizacion")
    List<Object[]> countByTipoOrganizacionGroup();

    @Query("SELECT o.activo, COUNT(o) FROM Organizacion o GROUP BY o.activo")
    List<Object[]> countByActivoGroup();

    // Existence checks
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Organizacion o WHERE o.nroNit = :nroNit")
    boolean existsByNroNit(@Param("nroNit") String nroNit);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Organizacion o WHERE o.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Organizacion o WHERE o.uuid = :uuid")
    boolean existsByUuid(@Param("uuid") String uuid);
}
