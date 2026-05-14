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
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "reserva_tarifa_detalles", indexes = {
        @Index(name = "idx_restar_det_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_restar_det_reserva", columnList = "reserva_id")
})
@SQLDelete(sql = "UPDATE reserva_tarifa_detalles SET estado = true WHERE id_reserva_tarifa_detalle = ?")
@Where(clause = "estado = false")
public class ReservaTarifaDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva_tarifa_detalle")
    private Long idReservaTarifaDetalle;

    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "tipo_linea", nullable = false, length = 30)
    private String tipoLinea; // BASE, CONCEPTO

    @Column(name = "nombre_concepto", length = 100)
    private String nombreConcepto;

    @Column(name = "tipo_cobro", length = 30)
    private String tipoCobro;

    @Column(name = "tipo_calculo", length = 30)
    private String tipoCalculo;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "regla_aplicada", columnDefinition = "TEXT")
    private String reglaAplicada;

    @Column(name = "datos_snapshot", columnDefinition = "TEXT")
    private String datosSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_id")
    private Tarifa tarifa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_concepto_id")
    private TarifaConcepto tarifaConcepto;

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
