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
@Table(name = "sistema_auditorias", indexes = {
        @Index(name = "idx_sisaudit_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_sisaudit_fecha", columnList = "fecha_accion"),
        @Index(name = "idx_sisaudit_usuario", columnList = "usuario_uuid"),
        @Index(name = "idx_sisaudit_modulo", columnList = "modulo"),
        @Index(name = "idx_sisaudit_entidad", columnList = "entidad_tipo, entidad_uuid")
})
@SQLDelete(sql = "UPDATE sistema_auditorias SET estado = true WHERE id_sistema_auditoria = ?")
@Where(clause = "estado = false")
public class SistemaAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sistema_auditoria")
    private Long idSistemaAuditoria;

    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_accion", nullable = false, updatable = false)
    private LocalDateTime fechaAccion;

    @Column(name = "usuario_uuid", length = 36)
    private String usuarioUuid;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    @Column(name = "modulo", nullable = false, length = 50)
    private String modulo;

    @Column(name = "entidad_tipo", length = 50)
    private String entidadTipo;

    @Column(name = "entidad_uuid", length = 50)
    private String entidadUuid;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "datos_cambio", columnDefinition = "TEXT")
    private String datosCambio;

    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.fechaAccion == null) {
            this.fechaAccion = LocalDateTime.now();
        }
    }
}
