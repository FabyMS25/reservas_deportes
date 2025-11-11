package com.gamq.deportes.repository;

import com.gamq.deportes.model.ClasificadorRuat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClasificadorRuatRepository extends JpaRepository<ClasificadorRuat, Long> {

    @Query("SELECT c FROM ClasificadorRuat c WHERE c.uuid = ?1")
    Optional<ClasificadorRuat> findByUuid(String uuid);

    // Find by codigo (unique)
    @Query("SELECT c FROM ClasificadorRuat c WHERE c.codigo = :codigo AND c.estado = false")
    Optional<ClasificadorRuat> findByCodigo(@Param("codigo") String codigo);

    // Find all active clasificadores
    @Query("SELECT c FROM ClasificadorRuat c WHERE c.estado = false AND c.activo = true ORDER BY c.codigo")
    List<ClasificadorRuat> findByActivoTrue();

    // Find by tipo reserva
//    @Query("SELECT c FROM ClasificadorRuat c WHERE c.tipoReserva = :tipoReserva AND c.estado = false ORDER BY c.codigo")
//    List<ClasificadorRuat> findByTipoReserva(@Param("tipoReserva") String tipoReserva);

    // Find by tipo reserva and active
//    @Query("SELECT c FROM ClasificadorRuat c WHERE c.tipoReserva = :tipoReserva AND c.estado = false AND c.activo = true ORDER BY c.codigo")
//    List<ClasificadorRuat> findByTipoReservaAndActivoTrue(@Param("tipoReserva") String tipoReserva);

    // Find by tipo arancel
    @Query("SELECT c FROM ClasificadorRuat c WHERE c.tipoArancel = :tipoArancel AND c.estado = false ORDER BY c.codigo")
    List<ClasificadorRuat> findByTipoArancel(@Param("tipoArancel") String tipoArancel);

    // Find by nombre containing (case insensitive)
    @Query("SELECT c FROM ClasificadorRuat c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND c.estado = false ORDER BY c.nombre")
    List<ClasificadorRuat> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    // Find all including inactive (for admin purposes)
    @Query("SELECT c FROM ClasificadorRuat c WHERE c.estado = false ORDER BY c.activo DESC, c.codigo")
    List<ClasificadorRuat> findAllActive();

    // Check if codigo exists (excluding current entity for updates)
    @Query("SELECT COUNT(c) > 0 FROM ClasificadorRuat c WHERE c.codigo = :codigo AND c.uuid != :excludeUuid AND c.estado = false")
    boolean existsByCodigoAndUuidNot(@Param("codigo") String codigo, @Param("excludeUuid") String excludeUuid);

    // Check if codigo exists
    @Query("SELECT COUNT(c) > 0 FROM ClasificadorRuat c WHERE c.codigo = :codigo AND c.estado = false")
    boolean existsByCodigo(@Param("codigo") String codigo);

    // Find by multiple criteria for advanced search
    @Query("SELECT c FROM ClasificadorRuat c WHERE " +
            "(:codigo IS NULL OR c.codigo = :codigo) AND " +
            "(:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:tipoArancel IS NULL OR c.tipoArancel = :tipoArancel) AND " +
            "(:activo IS NULL OR c.activo = :activo) AND " +
            "c.estado = false " +
            "ORDER BY c.codigo")
    List<ClasificadorRuat> findByCriteria(@Param("codigo") String codigo,
                                          @Param("nombre") String nombre,
                                          @Param("tipoArancel") String tipoArancel,
                                          @Param("activo") Boolean activo);

    // Count active clasificadores
    @Query("SELECT COUNT(c) FROM ClasificadorRuat c WHERE c.estado = false AND c.activo = true")
    long countActive();

    // Find by list of UUIDs
    @Query("SELECT c FROM ClasificadorRuat c WHERE c.uuid IN :uuids AND c.estado = false ORDER BY c.codigo")
    List<ClasificadorRuat> findByUuidIn(@Param("uuids") List<String> uuids);
}