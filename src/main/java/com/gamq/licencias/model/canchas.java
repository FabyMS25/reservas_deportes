package com.gamq.licencias.model;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;
import org.locationtech.jts.geom.Point;
//import com.vividsolutions.jts.geom.Point;

import javax.persistence.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "canchas", indexes = @Index(name = "idx_can_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE canchas SET estado=true WHERE id_cancha=?")
@Where(clause = "estado = false")

public class canchas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cancha")
    private Long idCancha;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "techado", nullable = false, length = 15)
    private String techado;
    @Column(name = "superficie", nullable = false,  length = 50)
    private String superficie;
    @Column(name = "ubicacion", nullable = false, length = 150)
    private String ubicacion;
    @Column(name = "distrito", nullable = false,  length = 50)
    private String distrito;
    @Column(name = "totalxhora", nullable = false)
    private Double totalxhora;

    @Column(name = "geo_ubicacion", columnDefinition = "Geometry")
    private Point geoUbicacion;
    @Column(name = "nombre", nullable = false,  length = 75)
    private String nombre;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
    public List<canchaDeportes> canchaDeportesList = new ArrayList<>();

    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
    public List<encargadoCancha> encargadoCanchaList = new ArrayList<>();

    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
    public List<horario> horarioList = new ArrayList<>();

    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
    public List<reservas> reservasList = new ArrayList<>();

    public canchas(String uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    public void initializeUuid() {
        this.setUuid(UUID.randomUUID().toString());
    }
}
