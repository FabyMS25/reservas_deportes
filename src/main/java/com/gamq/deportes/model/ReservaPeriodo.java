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
@Table(name = "reservas_periodo", indexes = {
        @Index(name = "idx_res_periodo_reserva", columnList = "id_reserva"),
        @Index(name = "idx_res_periodo_estudiante", columnList = "id_estudiante_escuela")
})
@SQLDelete(sql = "UPDATE reservas_periodo SET estado = true WHERE id_reserva_periodo = ?")
@Where(clause = "estado = false")
public class ReservaPeriodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva_periodo")
    private Long idReservaPeriodo;

    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Period information
    @Column(name = "mes")
    private Integer mes; // 1-12

    @Column(name = "año")
    private Integer año;

    // School context
    @Column(name = "matricula", length = 50)
    private String matricula;

    @Column(name = "disciplina", length = 100)
    private String disciplina;

    @Column(name = "entrenador", length = 100)
    private String entrenador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante_escuela", nullable = false)
    private EstudianteEscuela estudianteEscuela;

    public ReservaPeriodo(String uuid) {
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
