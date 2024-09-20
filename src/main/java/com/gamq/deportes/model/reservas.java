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
@Table(name = "reservas", indexes = @Index(name = "idx_res_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE reservas SET estado=true WHERE id_reserva=?")
@Where(clause = "estado = false")
public class reservas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long idReserva;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "fecha_created", nullable = false)
    private Date fechaCreated;

    @Column(name = "fecha_reserva", nullable = false)
    private Date fechaReserva;
    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;
    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;
    @Column(name = "total_pago", nullable = false)
    private BigDecimal totalPago;
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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_cancha")
    public canchas cancha;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_contribuyente")
    public contribuyentes contribuyente;

    @OneToMany(mappedBy = "reservas", cascade = CascadeType.ALL)
    public List<pagos> pagosList = new ArrayList<>();




    public reservas(String uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    public void initializeUuid() {
        this.setUuid(UUID.randomUUID().toString());
    }
}
