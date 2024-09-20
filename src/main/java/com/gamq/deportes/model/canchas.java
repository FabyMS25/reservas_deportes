package com.gamq.deportes.model;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
//import com.vividsolutions.jts.geom.Point;

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
    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_complejo")
    public complejo complejo;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_superficie")
    public Superficie superficie;

    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
    public List<Tarifas> tarifasList = new ArrayList<>();

    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
    public List<horario> horarioList = new ArrayList<>();

    //@OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
    //public List<reservas> reservasList = new ArrayList<>();


    public canchas(String uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    public void initializeUuid() {
        this.setUuid(UUID.randomUUID().toString());
    }
}
