package com.gamq.deportes.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDate;
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
@Table(name = "organizaciones",
        indexes = {
                @Index(name = "idx_org_uuid", columnList = "uuid", unique = true),
                @Index(name = "idx_org_nombre", columnList = "nombre")
        })
@SQLDelete(sql = "UPDATE organizaciones SET estado=true WHERE id_organizacion=?")
@Where(clause = "estado = false")
public class Organizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_organizacion")
    private Long idOrganizacion;

    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "tipo_organizacion", nullable = false, length = 50)
    private String tipoOrganizacion; // asociacion_deportiva, club_deportivo, unidad_educativa, empresa_privada, institucion_publica
    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;
    @Column(name = "telefono", length = 20)
    private String telefono;
    @Column(name = "email", length = 255)
    private String email;
    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;
    @Column(name = "personeria_juridica", length = 100)
    private String personeriaJuridica;
    @Column(name = "nro_nit", length = 20)
    private String nroNit;
    @Column(name = "activo", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean activo = true;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_representante", nullable = false)
    private Contribuyente representante;

//    @OneToMany(mappedBy = "organizacion", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<Reserva> reservasList = new ArrayList<>();

    public Organizacion(String uuid) {
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
        if (this.activo == null) {
            this.activo = true;
        }
    }

    // Helper methods
    public boolean estaActiva() {
        return !estado && activo;
    }
}
