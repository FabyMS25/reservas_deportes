package com.gamq.licencias.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "horario", indexes = @Index(name = "idx_hor_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE horario SET estado=true WHERE id_horario=?")
@Where(clause = "estado = false")
public class horario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private Long idHorario;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "dia", nullable = false, length = 15)
    private String dia;
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;
    @Column(name = "abierto", nullable = false)
    private boolean abierto;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_cancha")
    public canchas cancha;


    public horario(String uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    public void initializeUuid() {
        this.setUuid(UUID.randomUUID().toString());
    }
}