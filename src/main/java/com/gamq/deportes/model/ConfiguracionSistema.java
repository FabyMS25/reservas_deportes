package com.gamq.deportes.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "configuracion_sistema", indexes = {
        @Index(name = "idx_configuracion_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_configuracion_clave", columnList = "clave"),
        @Index(name = "idx_configuracion_scope", columnList = "clave, aplica_a, referencia_uuid"),
        @Index(name = "idx_configuracion_categoria", columnList = "categoria")
})
@SQLDelete(sql = "UPDATE configuracion_sistema SET estado = true WHERE id_configuracion = ?")
@Where(clause = "estado = false")
public class ConfiguracionSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuracion")
    private Long idConfiguracion;

    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Identification
    @Column(name = "clave", nullable = false, length = 100)
    private String clave;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;

    // Values (flexible storage)
    @Column(name = "valor_numerico")
    private Double valorNumerico;

    @Column(name = "valor_texto", columnDefinition = "TEXT")
    private String valorTexto;

    @Column(name = "valor_boolean", columnDefinition = "BOOLEAN")
    private Boolean valorBoolean;

    @Column(name = "valor_json", columnDefinition = "TEXT")
    private String valorJson;

    // Metadata
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida;

    // Validation
    @Column(name = "valor_minimo")
    private Double valorMinimo;

    @Column(name = "valor_maximo")
    private Double valorMaximo;

    @Column(name = "valores_posibles", columnDefinition = "TEXT")
    private String valoresPosibles;

    // Control
    @Column(name = "modificable_por_usuario", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean modificablePorUsuario = false;

    @Column(name = "requiere_aprobacion", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean requiereAprobacion = false;

    @Column(name = "activo", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean activo = true;

    // Audit
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    @Column(name = "modificado_por", length = 50)
    private String modificadoPor;

    // Application scope
    @Column(name = "aplica_a", length = 50)
    private String aplicaA;

    @Column(name = "referencia_uuid", length = 50)
    private String referenciaUuid;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.fechaActualizacion == null) {
            this.fechaActualizacion = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Helper methods to get typed values
    public String getValorComoString() {
        if (valorTexto != null) return valorTexto;
        if (valorNumerico != null) return String.valueOf(valorNumerico);
        if (valorBoolean != null) return String.valueOf(valorBoolean);
        if (valorJson != null) return valorJson;
        return null;
    }

    public Double getValorComoDouble() {
        if (valorNumerico != null) return valorNumerico;
        if (valorTexto != null) {
            try {
                return Double.parseDouble(valorTexto);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public Boolean getValorComoBoolean() {
        if (valorBoolean != null) return valorBoolean;
        if (valorTexto != null) {
            return Boolean.parseBoolean(valorTexto);
        }
        return null;
    }

    public boolean isConfiguracionActiva() {
        return Boolean.TRUE.equals(activo) && !estado;
    }
}
