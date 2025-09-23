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
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "reservas_programadas", indexes = @Index(name = "idx_resp_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE reservas_programadas SET estado=true WHERE id_reserva=?")
@Where(clause = "estado = false")
public class ReservasProgramadas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservap")
    private Long idReservap;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "fecha_created", nullable = false)
    private Date fechaCreated;
    @Column(name = "fecha_reserva", nullable = false)
    private Date fechaReserva;

    @Column(name = "categoria", nullable = false)
    private String categoria;
    @Column(name = "superficie", nullable = false)
    private String superficie;


    @Column(name = "total_personas", nullable = false)
    private int totalPersonas;

    @Column(name = "numero_tasa", nullable = false)
    private String numeroTasa;

    @Column(name = "estado_reserva", nullable = false)
    private String estadoReserva;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name="id_cancha")
    public canchas cancha;

    @ManyToOne(cascade ={CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name="id_contribuyente")
    public contribuyentes contribuyente;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name="id_tablero")
    public Tablero tablero;

    @PrePersist
    public void initializeUuid() {
        this.setUuid(UUID.randomUUID().toString());
    }
}
