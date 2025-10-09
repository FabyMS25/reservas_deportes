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

    @Column(name = "estado_actual", length = 20)
    private String estadoActual;// activo, mantenimiento, cerrado
    @Column(name = "codigo", length = 50)
    private String codigo;
    @Column(name = "nombre", nullable = false, length = 250)
    private String nombre;

    @Column(name = "categoria", length = 10)
    private String categoria; // 'A', 'B', 'ELITE'
//    @Column(name = "tipo", nullable = false, length = 50)
//    private String tipo;
//    @Column(name = "tipo_estructura", length = 20)
//    private String tipoEstructura; // techado, abierto, cerrado
    
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


    @Column(name = "acceso_libre", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean accesoLibre = false;
    @Column(name = "requiere_aprobacion", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean requiereAprobacion = false;
    @Column(name = "horas_minima_aprobacion")
    private Integer horasMinimaAprobacion;
    @Column(name = "max_duracion_auto_aprobacion")
    private Integer maxDuracionAutoAprobacion;
    @Column(name = "max_personas_auto_aprobacion")
    private Integer maxPersonasAutoAprobacion;

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
//    private List<CategoriaDeportiva> categorias = new ArrayList<>();

//    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
//    public List<Tarifas> tarifasList = new ArrayList<>();
//    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
//    public List<HorarioFijo> horarioList = new ArrayList<>();
//    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
//    public List<Reserva> reservasList = new ArrayList<>();
//    @OneToMany(mappedBy = "campo", cascade = CascadeType.ALL)
//    public List<ReservaRecurrente> reservasProgramadasList = new ArrayList<>();

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

    public boolean requiereAprobacionReserva(Reserva reserva) {
        if (!Boolean.TRUE.equals(this.requiereAprobacion)) {
            return false;
        }
        return !esReservaAutoAprobable(reserva);
    }

    public boolean esReservaAutoAprobable(Reserva reserva) {
        if (horasMinimaAprobacion != null) {
            LocalDateTime ahora = LocalDateTime.now();
            long horasHastaReserva = java.time.Duration.between(ahora, reserva.getFechaReserva()).toHours();
            if (horasHastaReserva >= horasMinimaAprobacion) {
                return true;
            }
        }
        if (maxPersonasAutoAprobacion != null && reserva.getReservaPuntual() != null) {
            Integer cantidadPersonas = reserva.getReservaPuntual().getCantidadPersonas();
            if (cantidadPersonas != null && cantidadPersonas <= maxPersonasAutoAprobacion) {
                return true;
            }
        }
        if (maxDuracionAutoAprobacion != null && reserva.getReservaPuntual() != null) {
            Integer duracion = reserva.getReservaPuntual().getDuracionHoras();
            if (duracion != null && duracion <= maxDuracionAutoAprobacion) {
                return true;
            }
        }
        return false;
    }
}