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
@Table(name = "horario_excepciones", indexes = {
        @Index(name = "idx_he_uuid", columnList = "uuid", unique = true)
})
@SQLDelete(sql = "UPDATE horario_excepciones SET estado = true WHERE id_horario_excepcion = ?")
@Where(clause = "estado = false")
public class HorarioExcepcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario_excepcion")
    private Long idHorarioExcepcion;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;
    @Column(name = "tipo_excepcion", nullable = false, length = 30)
    private String tipoExcepcion; // cerrado, horario_especial, mantenimiento, evento_municipal
    @Column(name = "hora_apertura")
    private LocalTime horaApertura;
    @Column(name = "hora_cierre")
    private LocalTime horaCierre;
    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

//    @ManyToOne(cascade = CascadeType.ALL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campo")
    public CampoDeportivo campoDeportivo;

    public HorarioExcepcion(String uuid) {
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
    }
}