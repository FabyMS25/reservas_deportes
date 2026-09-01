package com.gamq.deportes.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "notificaciones_admin", indexes = {
        @Index(name = "idx_notificacion_admin_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_notificacion_admin_fecha", columnList = "fecha_creacion"),
        @Index(name = "idx_notificacion_admin_referencia", columnList = "tipo, referencia_tipo, referencia_uuid")
})
@SQLDelete(sql = "UPDATE notificaciones_admin SET estado = true WHERE id_notificacion_admin = ?")
@Where(clause = "estado = false")
public class NotificacionAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion_admin")
    private Long idNotificacionAdmin;

    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;

    @Column(name = "estado", nullable = false)
    private boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "tipo", nullable = false, length = 80)
    private String tipo;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "prioridad", nullable = false, length = 20)
    private String prioridad = "INFO";

    @Column(name = "canal", nullable = false, length = 20)
    private String canal = "APP";

    @Column(name = "referencia_tipo", length = 50)
    private String referenciaTipo;

    @Column(name = "referencia_uuid", length = 50)
    private String referenciaUuid;

    @Column(name = "accion_url", length = 300)
    private String accionUrl;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "usuario_excluido_uuid", length = 50)
    private String usuarioExcluidoUuid;

    @PrePersist
    public void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID().toString();
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (prioridad == null) prioridad = "INFO";
        if (canal == null) canal = "APP";
    }
}
