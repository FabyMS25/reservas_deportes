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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "tarifas", indexes = @Index(name = "idx_tar_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE tarifas SET estado=true WHERE id_tarifa=?")
@Where(clause = "estado = false")
public class Tarifa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarifa")
    private Long idTarifa;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    @Column(name = "desde")
    private LocalTime desde;
    @Column(name = "hasta")
    private LocalTime hasta;
    @Column(name = "activo", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean activo = true;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "tipo_cobro", nullable = false, length = 20)
    private String tipoCobro; // 'por_hora' | 'por_persona' | 'por_evento' | 'por_sesion' | 'mensualidad' | 'por_club'
    @Column(name = "frecuencia", length = 20)
    private String frecuencia; // 'diaria' | 'semanal' | 'mensual'
    @Column(name = "sesiones_incluidas", precision = 5, scale = 0)
    private BigDecimal sesionesIncluidas;

    //    @ManyToOne(cascade = CascadeType.ALL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campo", nullable = false)
    private CampoDeportivo campoDeportivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_clasificador_ruat", nullable = false)
    private ClasificadorRuat clasificadorRuat;

    @OneToMany(mappedBy = "tarifa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TarifaConcepto> conceptos = new ArrayList<>();
    @OneToMany(mappedBy = "tarifa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TarifaCategoria> categoriasList = new ArrayList<>();
    public Tarifa(String uuid) {
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
    }
}
