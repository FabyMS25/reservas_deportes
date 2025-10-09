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
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "reservas_recurrentes", indexes = @Index(name = "idx_resr_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE reservas_recurrentes SET estado=true WHERE id_reserva_recurrente=?")
@Where(clause = "estado = false")
public class ReservaRecurrente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva_recurrente")
    private Long idReservaRecurrente;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "tipo_recurrencia", nullable = false, length = 20)
    private String tipoRecurrencia;
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    @Column(name = "cantidad_personas")
    private Integer cantidadPersonas;
    @Column(name = "dias_semana")
    private Integer diasSemana;
    @Column(name = "cantidad_sesiones")
    private Integer cantidadSesiones;
    @Column(name = "duracion_sesuib_horas")
    private Integer duracionSesionHoras;
    @Column(name = "tarifa_mensual")
    private Integer tarifaMensual;
    @Column(name = "activo", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean activo = true;
//    @Column(name = "estado_reserva", nullable = false)
//    private String estadoReserva;
    @Column(name = "categoria")
    private String categoria;
    @Column(name = "superficie")
    private String superficie;
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;


    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name="id_tablero")
    public Tablero tablero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    @PrePersist
    public void initializeDates() {
        if (this.uuid == null) {
            this.setUuid(UUID.randomUUID().toString());
        }
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.activo == null) {
            this.activo = true;
        }
    }
}
