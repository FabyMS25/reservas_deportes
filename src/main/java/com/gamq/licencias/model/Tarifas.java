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
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "tarifas", indexes = @Index(name = "idx_tar_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE tarifas SET estado=true WHERE id_tarifa=?")
@Where(clause = "estado = false")


public class Tarifas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarifa")
    private Long idTarifa;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;
    @Column(name = "desde", nullable = false)
    private LocalTime desde;
    @Column(name = "hasta", nullable = false)
    private LocalTime hasta;
    @Column(name = "costo", nullable = false)
    private BigDecimal costo;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_cancha")
    public canchas cancha;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="id_cancha")
//    public canchas cancha;

    @PrePersist
    public void initializeUuid() {
        this.setUuid(UUID.randomUUID().toString());
    }

}
