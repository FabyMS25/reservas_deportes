package com.gamq.deportes.model;

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
@Table(name = "estudiantes_escuela",
        indexes = {
                @Index(name = "idx_est_uuid", columnList = "uuid", unique = true),
                @Index(name = "idx_est_matricula", columnList = "matricula", unique = true),
                @Index(name = "idx_est_numero_documento", columnList = "numero_documento"),
                @Index(name = "idx_est_activo", columnList = "activo"),
                @Index(name = "idx_est_contribuyente", columnList = "id_contribuyente")
        })
@SQLDelete(sql = "UPDATE estudiantes_escuela SET estado=true WHERE id_estudiante=?")
@Where(clause = "estado = false")
public class EstudianteEscuela {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Long idEstudiante;

    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Student Personal Information
    @Column(name = "nombre_completo", nullable = false, length = 255)
    private String nombreCompleto;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "genero", length = 5)
    private String genero; // M, F

    @Column(name = "numero_documento", length = 20)
    private String numeroDocumento;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email", length = 255)
    private String email;

    // School Enrollment Information
    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDate fechaInscripcion;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    @Column(name = "matricula", nullable = false, unique = true, length = 50)
    private String matricula;

    @Column(name = "activo", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean activo = true;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contribuyente", nullable = false)
    private Contribuyente contribuyenteDto; // representante o tutor

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id_organizacion", nullable = false)
//    private Organizacion organizacionDto;

    @OneToMany(mappedBy = "estudianteEscuela", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Reserva> mensualidadesDtoList = new ArrayList<>();

    public EstudianteEscuela(String uuid) {
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
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.fechaInscripcion == null) {
            this.fechaInscripcion = LocalDate.now();
        }
    }

    // Helper methods
    public boolean estaActivo() {
        return !estado && Boolean.TRUE.equals(activo) && fechaBaja == null;
    }

    public Integer calcularEdad() {
        if (fechaNacimiento == null) {
            return null;
        }
        return LocalDate.now().getYear() - fechaNacimiento.getYear();
    }

//    public boolean tieneMensualidadesPendientes() {
//        return mensualidadesDtoList != null &&
//                mensualidadesDtoList.stream()
//                        .anyMatch(reserva -> reserva.getEstadoPago() != null &&
//                                "PENDIENTE".equals(reserva.getEstadoPago()));
//    }

    public void darDeBaja() {
        this.activo = false;
        this.fechaBaja = LocalDate.now();
    }

    public void reactivar() {
        this.activo = true;
        this.fechaBaja = null;
    }
}