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
@Table(name = "campos_deportivos", indexes = @Index(name = "idx_campo_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE campos_deportivos SET estado = true WHERE id_campo = ?")
@Where(clause = "estado = false")
public class CampoDeportivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_campo")
    private Long idCampo;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;
    @Column(name = "nombre", nullable = false, length = 250)
    private String nombre;
//    @Column(name = "tipo", nullable = false, length = 50)
//    private String tipo;
//    @Column(name = "tipo_estructura", length = 20)
//    private String tipoEstructura; // techado, abierto, cerrado

    @Column(name = "estado_actual", length = 20)
    private String estadoActual; // activo, mantenimiento, cerrado

    @Column(name = "dimensiones", length = 100)
    private String dimensiones;
    @Column(name = "capacidad_personas")
    private Integer capacidadPersonas = 0;
    @Column(name = "tiene_iluminacion", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean tieneIluminacion = false;
//    @Column(name = "tiene_vestuarios", columnDefinition = "BOOLEAN DEFAULT false")
//    private Boolean tieneVestuarios = false;
//    @Column(name = "tiene_graderias", columnDefinition = "BOOLEAN DEFAULT false")
//    private Boolean tieneGraderias = false;
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

//    @ManyToOne(cascade = CascadeType.ALL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_complejo")
    public ComplejoDeportivo complejo;
//    @ManyToOne(cascade = CascadeType.ALL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_superficie")
    public Superficie superficie;

    @OneToMany(mappedBy = "campoDeportivo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Tarifa> tarifasList = new ArrayList<>();
    @OneToMany(mappedBy = "campoDeportivo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<HorarioFijo> horariosFijosList = new ArrayList<>();
    @OneToMany(mappedBy = "campoDeportivo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<HorarioExcepcion> horariosExcepcionesList = new ArrayList<>();
    @OneToMany(mappedBy = "campoDeportivo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Reserva> reservasList = new ArrayList<>();
//    @OneToMany(mappedBy = "campo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Categoria> categorias = new ArrayList<>();

//    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
//    public List<Tarifas> tarifasList = new ArrayList<>();
//    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
//    public List<HorarioFijo> horarioList = new ArrayList<>();
//    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
//    public List<Reserva> reservasList = new ArrayList<>();
//    @OneToMany(mappedBy = "campo", cascade = CascadeType.ALL)
//    public List<ReservasProgramadas> reservasProgramadasList = new ArrayList<>();

    public CampoDeportivo(String uuid) {
        this.uuid = uuid;
    }
//    @PrePersist
//    public void initializeUuid() {
//        this.setUuid(UUID.randomUUID().toString());
//    }

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