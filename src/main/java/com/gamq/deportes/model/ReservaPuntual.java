package com.gamq.deportes.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "reservas_puntuales", indexes = @Index(name = "idx_resp_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE reservas_puntuales SET estado = true WHERE id_reserva_puntual = ?")
@Where(clause = "estado = false")
public class ReservaPuntual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva_puntual")
    private Long idReservaPuntual;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;


    @Column(name = "fecha_uso")
    private LocalDate fechaUso;
    @Column(name = "fecha_uso_real")
    private LocalDateTime fechaUsoReal;
    @Column(name = "hora_inicio")
    private LocalTime horaInicio;
    @Column(name = "hora_fin")
    private LocalTime horaFin;
    @Column(name = "duracion_horas")
    private Integer duracionHoras;
    @Column(name = "cantidad_personas")
    private Integer cantidadPersonas;
    @Column(name = "precio_base_aplicado", precision = 10, scale = 2)
    private BigDecimal precioBaseAplicado;
    @Column(name = "categoria", nullable = false)
    private String categoria;
    @Column(name = "superficie", nullable = false)
    private String superficie;
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    @Column(name = "periodo")
    private String periodo;
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
    }
}
