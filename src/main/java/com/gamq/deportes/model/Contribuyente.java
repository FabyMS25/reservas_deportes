package com.gamq.deportes.model;

import com.gamq.deportes.dto.EstudianteEscuelaDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDate;
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
@Table(name = "contribuyentes",
        indexes = {
                @Index(name = "idx_cont_uuid", columnList = "uuid", unique = true),
                @Index(name = "idx_cont_email", columnList = "email", unique = true),
                @Index(name = "idx_cont_user_name", columnList = "user_name", unique = true),
                @Index(name = "idx_cont_codigo", columnList = "codigo_contribuyente", unique = true)
        })
@SQLDelete(sql = "UPDATE contribuyentes SET estado=true WHERE id_contribuyente=?")
@Where(clause = "estado = false")
public class Contribuyente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contribuyente")
    private Long idContribuyente;

    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    // Authentication
    @Column(name = "user_name", nullable = false, length = 150)
    private String userName;

    @Column(name = "password", nullable = false, length = 150)
    private String password;

    @Column(name = "estado_usuario", nullable = false, length = 30)
    private String estadoUsuario;

    // RUAT Integration
    @Column(name = "estado_ruat", length = 20)
    private String estadoRuat; // ACTIVO, BAJA from RUAT

    @Column(name = "verificado_ruat", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean verificadoRuat = false;

    @Column(name = "fecha_verificacion_ruat")
    private LocalDateTime fechaVerificacionRuat;

    @Column(name = "tipo_contribuyente", nullable = false, length = 30)
    private String tipoContribuyente; // NA=Natural, JU=Juridica

    @Column(name = "codigo_contribuyente", length = 30)
    private String codigoContribuyente;

    @Column(name = "tipo_documento", nullable = false, length = 30)
    private String tipoDocumento; // CI, CE

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(name = "expedido", length = 20)
    private String expedido; // Department code 1-9

    @Column(name = "nro_nit", length = 20)
    private String nroNit;

    // Personal Information
    @Column(name = "nombre", nullable = false, length = 250)
    private String nombre;

    @Column(name = "primer_apellido", nullable = false, length = 100)
    private String primerApellido;

    @Column(name = "segundo_apellido", length = 100)
    private String segundoApellido;

    @Column(name = "apellido_esposo", length = 100)
    private String apellidoEsposo;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "estado_civil", length = 5)
    private String estadoCivil; // SO, CA, VI, DI

    @Column(name = "genero", length = 5)
    private String genero; // F,M

    // Contact Information
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "direccion", length = 250)
    private String direccion;

    // Special categories and benefits
    @Column(name = "es_deportista_elite", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean esDeportistaElite = false;
    @Column(name = "registro_unico_deportivo", columnDefinition = "TEXT")
    private String registroUnicoDeportivo;

    @Column(name = "fecha_ultimo_uso_gratuito")
    private LocalDate fechaUltimoUsoGratuito;

    @Column(name = "observaciones_municipales", columnDefinition = "TEXT")
    private String observacionesMunicipales;

    // Sanctions
    @Column(name = "esta_sancionado", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean estaSancionado = false;

    @Column(name = "fecha_fin_sancion")
    private LocalDateTime fechaFinSancion;

    @Column(name = "motivo_sancion", columnDefinition = "TEXT")
    private String motivoSancion;

    // Computed fields (transient - not persisted in database)
    @Transient
    private String nombreCompleto;

    @Transient
    private Boolean puedeReservarGratis;

    @OneToMany(mappedBy = "contribuyente", fetch = FetchType.LAZY)
    private List<OrganizacionRepresentante> organizacionRepresentanteLinks = new ArrayList<>();

    // Relations
    @OneToMany(mappedBy = "contribuyente", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Reserva> reservasList = new ArrayList<>();

    @OneToMany(mappedBy = "representante", fetch = FetchType.LAZY)
    private List<Organizacion> organizacionesRepresentadas = new ArrayList<>();

    @OneToMany(mappedBy = "contribuyenteDto", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EstudianteEscuela> estudianteEnEscuelas = new ArrayList<>();

    public Contribuyente(String uuid) {
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
        if (this.fechaActualizacion == null) {
            this.fechaActualizacion = LocalDateTime.now();
        }
        if (this.estadoUsuario == null) {
            this.estadoUsuario = "ACTIVO";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public String getNombreCompleto() {
        return String.format("%s %s %s",
                nombre != null ? nombre : "",
                primerApellido != null ? primerApellido : "",
                segundoApellido != null ? segundoApellido : "").trim();
    }

    public boolean puedeReservarGratis() {
        return Boolean.TRUE.equals(esDeportistaElite);
    }

    public boolean estaActivo() {
        return !estado && "ACTIVO".equals(estadoUsuario) &&
                (estadoRuat == null || "ACTIVO".equals(estadoRuat));
    }
}
