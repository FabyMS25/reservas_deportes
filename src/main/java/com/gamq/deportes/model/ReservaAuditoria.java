package com.gamq.deportes.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "reservas_auditorias", indexes = {
        @Index(name = "idx_resaudit_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_resaudit_reserva", columnList = "reserva_id"),
        @Index(name = "idx_resaudit_fecha", columnList = "fecha_accion")
})
public class ReservaAuditoria { // Using Spanish name as requested
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Column(name = "accion", nullable = false, length = 30)
    private String accion; // CREATE, UPDATE, DELETE, APPROVE, REJECT, etc.

    @Column(name = "estado_anterior", length = 50)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 50)
    private String estadoNuevo;

    @Column(name = "usuario_uuid", length = 36)
    private String usuarioUuid; // External user UUID

    @Column(name = "datos_cambio", columnDefinition = "TEXT")
    private String datosCambio; // JSON with changed data

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;

    @Column(name = "fecha_accion", nullable = false, updatable = false)
    private LocalDateTime fechaAccion;

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
