package com.gamq.licencias.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "pagos", indexes = @Index(name = "idx_pag_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE pagos SET estado=true WHERE id_pago=?")
@Where(clause = "estado = false")
public class pagos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "fecha_pago", nullable = false)
    private Date fechaPago;
    @Column(name = "monto_pago", nullable = false)
    private BigDecimal montoPago;
    @Column(name = "numero_tasa", nullable = false)
    private String numeroTasa;
    @Column(name = "entidad_financiera", nullable = false)
    private String entidadFinanciera;
    @Column(name = "folio", nullable = false)
    private String folio;
    @Column(name = "formaPago", nullable = false)
    private String formaPago;
    @Column(name = "observacion", nullable = false)
    private String observacion;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_reserva")
    public reservas reservas;

    public pagos(String uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    public void initializeUuid() {
        this.setUuid(UUID.randomUUID().toString());
    }
}
