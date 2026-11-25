package com.gamq.deportes.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Rango de talonarios/boletas de una actividad Borderoux.
 * Los importes se recalculan siempre en el servidor.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "reservas_borderoux_talonarios", indexes = {
        @Index(name = "idx_borderoux_tal_actividad", columnList = "id_reserva_borderoux_actividad")
})
@SQLDelete(sql = "UPDATE reservas_borderoux_talonarios SET estado = true WHERE id_reserva_borderoux_talonario = ?")
@SQLRestriction("estado = false")
public class ReservaBorderouxTalonario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva_borderoux_talonario")
    private Long idReservaBorderouxTalonario;

    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "precio_boleta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBoleta;

    @Column(name = "numero_inicial", nullable = false)
    private Integer numeroInicial;

    @Column(name = "numero_final", nullable = false)
    private Integer numeroFinal;

    @Column(name = "serie_talonario", length = 100)
    private String serieTalonario;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "cantidad_anuladas", nullable = false)
    private Integer cantidadAnuladas;

    @Column(name = "cantidad_vendida", nullable = false)
    private Integer cantidadVendida;

    @Column(name = "total_boletas", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalBoletas;

    @Column(name = "monto_borderoux", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoBorderoux;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva_borderoux_actividad", nullable = false)
    private ReservaBorderouxActividad actividad;

    public ReservaBorderouxTalonario(String uuid) {
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
