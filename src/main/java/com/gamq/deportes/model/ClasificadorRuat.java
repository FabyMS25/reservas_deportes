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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "clasificadores_ruat", indexes = {
        @Index(name = "idx_clasruat_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_clasruat_codigo", columnList = "codigo", unique = true)
})
@SQLDelete(sql = "UPDATE clasificadores_ruat SET estado = true WHERE id_clasificador_ruat = ?")
@Where(clause = "estado = false")
public class ClasificadorRuat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clasificador_ruat")
    private Long idClasificadorRuat;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "codigo", unique = true, nullable = false, length = 20)
    private String codigo;
    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    @Column(name = "tipo_reserva", nullable = false, length = 20)
//    private String tipoReserva; // por_hora, por_persona, por_evento, mensualidad
//    @Column(name = "tipo_arancel", nullable = false, length = 10)
    private String tipoArancel = "DI"; // DI = Discrecional
    @Column(name = "activo", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean activo = true;

    @OneToMany(mappedBy = "clasificadorRuat", cascade = CascadeType.ALL)
    private List<Tarifa> tarifasList = new ArrayList<>();

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
