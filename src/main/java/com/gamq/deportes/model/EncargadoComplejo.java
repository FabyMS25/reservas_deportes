package com.gamq.deportes.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "encargados_complejo", indexes = {
        @Index(name = "idx_enc_uuid", columnList = "uuid", unique = true)
})
@SQLDelete(sql = "UPDATE encargados_complejo SET estado = true WHERE id_encargado_complejo = ?")
@Where(clause = "estado = false")
public class EncargadoComplejo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_encargado_complejo")
    private Long idEncargadoComplejo;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "user_uuid", nullable = false, length = 50)
    private String userUuid;
    @Column(name = "avatar", nullable = false, length = 250)
    private String avatar;
    @Column(name = "activo", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean activo = true;
    @Column(name = "nombre", nullable = false, length = 250)
    private String nombre;
    @Column(name = "telefono", length = 20)
    private String telefono;
    @Column(name = "fecha_asignacion")
    private LocalDate fechaAsignacion;
    @Column(name = "fecha_fin_asignacion")
    private LocalDate fechaFinAsignacion;
    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;
    @Column(name = "hora_salida")
    private LocalTime horaSalida;

//    @ManyToOne(cascade = CascadeType.ALL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_complejo")
    public ComplejoDeportivo complejo;
    public EncargadoComplejo(String uuid) {
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
        if (this.fechaAsignacion == null) {
            this.fechaAsignacion = LocalDate.now();
        }
        if (this.activo == null) {
            this.activo = true;
        }
    }
}