package com.gamq.deportes.model;

import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "notificaciones", indexes = {
        @Index(name = "idx_notificacion_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_notificacion_usuario", columnList = "usuario_uuid"),
        @Index(name = "idx_notificacion_usuario_lectura", columnList = "usuario_uuid, estado_lectura"),
        @Index(name = "idx_notificacion_referencia", columnList = "referencia_tipo, referencia_uuid"),
        @Index(name = "uk_notificacion_entrega_admin", columnList = "usuario_uuid, notificacion_admin_uuid", unique = true)
})
@SQLDelete(sql = "UPDATE notificaciones SET estado = true WHERE id_notificacion = ?")
@Where(clause = "estado = false")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long idNotificacion;

    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "usuario_uuid", nullable = false, length = 50)
    private String usuarioUuid;

    @Column(name = "tipo", nullable = false, length = 80)
    private String tipo;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "estado_lectura", nullable = false, length = 20)
    private String estadoLectura = "NO_LEIDA";

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

    @Column(name = "programada_para")
    private LocalDateTime programadaPara;

    @Column(name = "enviada_en")
    private LocalDateTime enviadaEn;

    @Column(name = "leida_en")
    private LocalDateTime leidaEn;

    @Column(name = "expira_en")
    private LocalDateTime expiraEn;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "notificacion_admin_uuid", length = 50)
    private String notificacionAdminUuid;

    public Notificacion(String uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.fechaActualizacion == null) {
            this.fechaActualizacion = LocalDateTime.now();
        }
        if (this.estadoLectura == null) {
            this.estadoLectura = "NO_LEIDA";
        }
        if (this.prioridad == null) {
            this.prioridad = "INFO";
        }
        if (this.canal == null) {
            this.canal = "APP";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
