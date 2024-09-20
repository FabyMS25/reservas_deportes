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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "complejo", indexes = @Index(name = "idx_com_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE complejo SET estado=true WHERE id_complejo=?")
@Where(clause = "estado = false")
public class complejo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_complejo")
    private Long idComplejo;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "nombre", nullable = false, length = 75)
    private String nombre;
    @Column(name = "techado", nullable = false, length = 15)
    private String techado;
    @Column(name = "ubicacion", nullable = false, length = 150)
    private String ubicacion;
    @Column(name = "distrito", nullable = false,  length = 50)
    private String distrito;

    @Column(name = "geo_ubicacion", columnDefinition = "Geometry")
    private Point geoUbicacion;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @OneToMany(mappedBy = "complejo", cascade = CascadeType.ALL)
    public List<encargadoComplejo> encargadoComplejoList= new ArrayList<>();

    @OneToMany(mappedBy = "complejo", cascade = CascadeType.ALL)
    public List<canchas> canchasList = new ArrayList<>();




    public complejo(String uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    public void initializeUuid() {
        this.setUuid(UUID.randomUUID().toString());
    }
}



