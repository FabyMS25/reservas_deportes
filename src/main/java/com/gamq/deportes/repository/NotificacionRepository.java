package com.gamq.deportes.repository;

import com.gamq.deportes.model.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    @Query("SELECT n FROM Notificacion n WHERE n.uuid = :uuid")
    Optional<Notificacion> findByUuid(@Param("uuid") String uuid);

    @Query("SELECT n FROM Notificacion n WHERE n.usuarioUuid = :usuarioUuid ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findByUsuarioUuid(@Param("usuarioUuid") String usuarioUuid);

    @Query("SELECT n FROM Notificacion n WHERE n.usuarioUuid = :usuarioUuid ORDER BY n.fechaCreacion DESC")
    Page<Notificacion> findByUsuarioUuid(@Param("usuarioUuid") String usuarioUuid, Pageable pageable);

    @Query("SELECT n FROM Notificacion n WHERE n.usuarioUuid = :usuarioUuid AND n.estadoLectura = 'NO_LEIDA' ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findNoLeidasByUsuarioUuid(@Param("usuarioUuid") String usuarioUuid);

    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.usuarioUuid = :usuarioUuid AND n.estadoLectura = 'NO_LEIDA'")
    Long countNoLeidasByUsuarioUuid(@Param("usuarioUuid") String usuarioUuid);

    @Query("SELECT n FROM Notificacion n WHERE n.usuarioUuid = :usuarioUuid AND n.tipo = :tipo AND n.referenciaTipo = :referenciaTipo AND n.referenciaUuid = :referenciaUuid")
    Optional<Notificacion> findDuplicada(
            @Param("usuarioUuid") String usuarioUuid,
            @Param("tipo") String tipo,
            @Param("referenciaTipo") String referenciaTipo,
            @Param("referenciaUuid") String referenciaUuid);

    @Query("SELECT n FROM Notificacion n WHERE n.programadaPara IS NOT NULL AND n.programadaPara <= :fecha AND n.enviadaEn IS NULL")
    List<Notificacion> findPendientesDeEnvio(@Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query("UPDATE Notificacion n SET n.estadoLectura = 'LEIDA', n.leidaEn = :fecha, n.fechaActualizacion = :fecha WHERE n.usuarioUuid = :usuarioUuid AND n.estadoLectura = 'NO_LEIDA'")
    int marcarTodasComoLeidas(@Param("usuarioUuid") String usuarioUuid, @Param("fecha") LocalDateTime fecha);
}
