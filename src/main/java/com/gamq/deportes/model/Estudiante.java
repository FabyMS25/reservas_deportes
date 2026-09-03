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
@Table(name = "estudiantes")
@SQLDelete(sql = "UPDATE estudiantes SET estado=true WHERE id_estudiante=?")
@Where(clause = "estado = false")
public class Estudiante {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Long idEstudiante;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", nullable = false)
    private boolean estado;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
    @Column(name = "nombre_completo", nullable = false, length = 255)
    private String nombreCompleto;
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    @Column(name = "genero", length = 5)
    private String genero;
    @Column(name = "numero_documento", length = 20)
    private String numeroDocumento;
    @Column(name = "telefono", length = 20)
    private String telefono;
    @Column(name = "email", length = 255)
    private String email;
    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY)
    private List<EstudianteEscuela> inscripciones = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID().toString();
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (fechaActualizacion == null) fechaActualizacion = LocalDateTime.now();
        normalizar();
    }

    @PreUpdate
    public void preUpdate() { fechaActualizacion = LocalDateTime.now(); normalizar(); }

    private void normalizar() {
        if (nombreCompleto != null) nombreCompleto = nombreCompleto.trim();
        if (numeroDocumento != null && numeroDocumento.trim().isEmpty()) numeroDocumento = null;
        if (email != null && email.trim().isEmpty()) email = null;
    }
}
