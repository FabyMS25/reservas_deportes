package com.gamq.deportes.repository;

import com.gamq.deportes.model.NotificacionAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionAdminRepository extends JpaRepository<NotificacionAdmin, Long> {

    @Query("SELECT n FROM NotificacionAdmin n WHERE n.fechaCreacion >= :desde ORDER BY n.fechaCreacion DESC")
    List<NotificacionAdmin> findRecientes(@Param("desde") LocalDateTime desde);

    @Query("SELECT n FROM NotificacionAdmin n WHERE n.tipo = :tipo AND n.referenciaTipo = :referenciaTipo AND n.referenciaUuid = :referenciaUuid")
    Optional<NotificacionAdmin> findDuplicada(
            @Param("tipo") String tipo,
            @Param("referenciaTipo") String referenciaTipo,
            @Param("referenciaUuid") String referenciaUuid);

    @Modifying
    @Query(value = "UPDATE notificaciones_admin SET estado = true WHERE estado = false AND fecha_creacion < :limite", nativeQuery = true)
    int archivarAnterioresA(@Param("limite") LocalDateTime limite);
}
