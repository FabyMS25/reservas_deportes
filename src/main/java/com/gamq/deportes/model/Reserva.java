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

    @Column(name = "public_id", unique = true, nullable = false)
    private UUID publicId;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(name = "fecha_limite_pago")
    private LocalDateTime fechaLimitePago;

    @Column(name = "cancelacion_automatica", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean cancelacionAutomatica = true;

    @Column(name = "motivo_cancelacion_automatica", columnDefinition = "TEXT")
    private String motivoCancelacionAutomatica;

    @Column(name = "autorizado_por", length = 36)
    private String autorizadoPor;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "fecha_rechazo")
    private LocalDateTime fechaRechazo;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "aprobado_por", length = 36)
    private String aprobadoPor;

    @Column(name = "creado_por", length = 36)
    private String creadoPor;

    @Column(name = "anulado_por", length = 36)
    private String anuladoPor;

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
    @JoinColumn(name = "id_tarifa")
    private Tarifa tarifa;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ReservaPuntual reservaPuntual;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ReservaRecurrente reservaRecurrente;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReservaPeriodo> reservasPeriodoList = new ArrayList<>();

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("idPago ASC")
    private List<Pago> pagosList = new ArrayList<>();

    // Same first non-deleted payment displayed in the list, without a collection join that duplicates rows.
    @org.hibernate.annotations.Formula("(select p.estado_pago from pagos p where p.id_reserva = id_reserva and p.estado = false order by p.id_pago asc limit 1)")
    private String estadoPagoOrden;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReservaAuditoria> auditoriasList = new ArrayList<>();

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReservaTarifaDetalle> tarifaDetallesList = new ArrayList<>();

    public Reserva(String uuid) {
        this.uuid = uuid;
    }

    public boolean esRecurrenteGratuitaDeEscuela() {
        return "recurrente".equalsIgnoreCase(tipoReserva) && Boolean.TRUE.equals(esReservaGratuita)
                && organizacion != null && "ESCUELA_MUNICIPAL".equalsIgnoreCase(organizacion.getTipoOrganizacion());
    }

    @PrePersist
    public void initializeUuidAndDates() {
        if (this.uuid == null) {
            this.setUuid(UUID.randomUUID().toString());
        }
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.fechaActualizacion == null) {
            this.fechaActualizacion = LocalDateTime.now();
        }
        if (this.cancelacionAutomatica == null) {
            this.cancelacionAutomatica = true;
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
