package com.gamq.licencias.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "encargado_complejo", indexes = @Index(name = "idx_encom_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE encargado_complejo SET estado=true WHERE id_encargado_complejo=?")
@Where(clause = "estado = false")

public class encargadoComplejo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_encargado_complejo")
    private Long idEncargadoComplejo;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "user_uuid", nullable = false, length = 50)
    private String userUuid;
    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;
    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;
    @Column(name = "telefono", nullable = false,  length = 12)
    private String telefono;

    @Column(name = "nombre", nullable = false,  length = 150)
    private String nombre;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_complejo")
    public complejo complejo;

    public encargadoComplejo(String uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    public void initializeUuid() {
        this.setUuid(UUID.randomUUID().toString());
    }


}
