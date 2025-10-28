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
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "pagos", indexes = @Index(name = "idx_pag_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE pagos SET estado = true WHERE id_pago = ?")
@Where(clause = "estado = false")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "numero_tasa", nullable = false, length = 50)
    private String numeroTasa;
    @Column(name = "tipo_tasa", nullable = false, length = 10)
    private String tipoTasa = "TO"; // TO = Tasa Ordinaria

    @Column(name = "folio", length = 50)
    private String folio;
    @Column(name = "forma_pago", length = 150)
    private String formaPago;
    @Column(name = "fecha_pago", length = 50) // DD/MM/YYYY from RUAT
    private String fechaPago;
    @Column(name = "entidad_financiera", length = 250)
    private String entidadFinanciera;
    @Column(name = "monto_pagado", precision = 10, scale = 2)
    private BigDecimal montoPagado;
    @Column(name = "observaciones_ruat", columnDefinition = "TEXT")
    private String observaciones;
    @Column(name = "estado_pago", length = 20)
    private String estadoPago = "pendiente";

    @Column(name = "ultima_consulta")
    private LocalDateTime ultimaConsulta;
    @Column(name = "respuesta_completa_ruat", columnDefinition = "TEXT")
    private String respuestaCompletaRuat;

    //    @ManyToOne(cascade = CascadeType.ALL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    public Pago(String uuid) {
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
        if (this.fechaActualizacion == null) {
            this.fechaActualizacion = LocalDateTime.now();
        }
    }
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
