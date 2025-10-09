package com.gamq.deportes.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.locationtech.jts.geom.Point;

import javax.persistence.*;
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
@Table(name = "complejos_deportivos", indexes = @Index(name = "idx_com_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE complejos_deportivos SET estado = true WHERE id_complejo = ?")
@Where(clause = "estado = false")
public class ComplejoDeportivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_complejo")
    private Long idComplejo;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "estado_actual", length = 20)
    private String estadoActual; // activo, mantenimiento, cerrado
    @Column(name = "nombre", nullable = false, length = 250)
    private String nombre;
    @Column(name = "telefono_principal", length = 20)
    private String telefonoPrincipal;
//    @Column(name = "techado", nullable = false, length = 15)
//    private String techado;
    @Column(name = "tipo_estructura", nullable = false, length = 20)
    private String tipoEstructura; // techado, abierto, cerrado
    @Column(name = "tiene_iluminacion", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean tieneIluminacion;
    @Column(name = "tiene_vestuarios", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean tieneVestuarios;
    @Column(name = "tiene_graderias", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean tieneGraderias;
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "distrito", nullable = false,  length = 50)
    private String distrito;
    @Column(name = "ubicacion", nullable = false, length = 250)
    private String ubicacion;
    @Column(name = "geo_ubicacion", columnDefinition = "Geometry")
    private Point geoUbicacion;
//    private Geometry geoUbicacion;
    @OneToMany(mappedBy = "complejo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CampoDeportivo> camposDeportivosList = new ArrayList<>();

    @OneToMany(mappedBy = "complejo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EncargadoComplejo> encargadosList = new ArrayList<>();

    public ComplejoDeportivo(String uuid) {
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
        if (this.estadoActual == null) {
            this.estadoActual = "activo";
        }
    }
}