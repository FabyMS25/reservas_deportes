package com.gamq.licencias.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.locationtech.jts.geom.Point;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "construccion", indexes = @Index(name = "idx_cons_uuid",
        columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE construccion SET estado=true WHERE id_construccion=?")
@Where(clause = "estado = false")
public class Construccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_construccion")
    private Long idConstruccion;

    @Column(name = "uuid", nullable = false, unique = true, length = 64)
    private String uuid;

    @Column(name = "cod_inmueble", nullable = true, length = 20)
    private String codInmueble;

    @Column(name = "ci_titular", nullable = true, length = 12)
    private String ciTitular;

    @Column(name = "nombre_titular", nullable = false, length = 100)
    private String nombreTitular;

    @Column(name = "app_titular", nullable = true, length = 150)
    private String appTitular;

    @Column(name = "apm_titular", nullable = true, length = 150)
    private String apmTitular;

    @Column(name = "direccion", nullable = false, length = 150)
    private String direccion;

    @Column(name = "superficie", nullable = true)
    private Double superficie;

    @Column(name = "observacion", nullable = true, length = 250)
    private String observacion;

    @Column(name = "geo_ubicacion", columnDefinition = "geometry(Point, 4326)")
    private Point geoUbicacion;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'", nullable = false)
    private boolean estado;

    public Construccion(String uuid) {
        this.uuid = uuid;
    }
    @PrePersist
    public void initializeUuid() {
        this.uuid=(UUID.randomUUID().toString());
    }
}
