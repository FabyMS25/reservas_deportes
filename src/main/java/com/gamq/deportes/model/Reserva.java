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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "reservas", indexes = {
        @Index(name = "idx_res_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_res_numero", columnList = "numero_reserva", unique = true)
})
@SQLDelete(sql = "UPDATE reservas SET estado = true WHERE id_reserva = ?")
@Where(clause = "estado = false")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long idReserva;

    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "fecha_rechazo")
    private LocalDateTime fechaRechazo;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "aprobado_por", length = 36)
    private String aprobadoPor;

    @Column(name = "estado_validacion", length = 20)
    private String estadoValidacion;

    @Column(name = "estado_reserva", nullable = false, length = 20)
    private String estadoReserva = "borrador";

    @Column(name = "tipo_reserva", nullable = false, length = 20)
    private String tipoReserva;

    @Column(name = "numero_reserva", unique = true, nullable = false, length = 50)
    private String numeroReserva;

    @Column(name = "numero_tasa", length = 50)
    private String numeroTasa;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "clasificador_ruat_id", nullable = false)
    private String clasificadorRuatId;

    @Column(name = "es_reserva_gratuita", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean esReservaGratuita = false;

    @Column(name = "motivo_gratuidad", columnDefinition = "TEXT")
    private String motivoGratuidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campo", nullable = false)
    private CampoDeportivo campoDeportivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contribuyente", nullable = false)
    private Contribuyente contribuyente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organizacion")
    private Organizacion organizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante_escuela")
    private EstudianteEscuela estudianteEscuela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarifa")
    private Tarifa tarifa;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ReservaPuntual reservaPuntual;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ReservaRecurrente reservaRecurrente;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ReservaPeriodo reservaPeriodo;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pago> pagosList = new ArrayList<>();

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReservaAuditoria> auditoriasList = new ArrayList<>();

    public Reserva(String uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    public void initializeUuidAndDates() {
        if (this.uuid == null) {
            this.setUuid(UUID.randomUUID().toString());
        }
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estadoReserva == null) {
            this.estadoReserva = "borrador";
        }
        if (this.esReservaGratuita == null) {
            this.esReservaGratuita = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}