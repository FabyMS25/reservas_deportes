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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "organizaciones",
        indexes = {
                @Index(name = "idx_org_uuid", columnList = "uuid", unique = true),
                @Index(name = "idx_org_nombre", columnList = "nombre"),
                @Index(name = "idx_org_codigo_escuela", columnList = "codigo_escuela")
        })
@SQLDelete(sql = "UPDATE organizaciones SET estado=true WHERE id_organizacion=?")
@Where(clause = "estado = false")
public class Organizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_organizacion")
    private Long idOrganizacion;

    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "activo", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean activo = true;

    // Organization Type and Legal Information
    @Column(name = "tipo_organizacion", nullable = false, length = 50)
    private String tipoOrganizacion; // ASOCIACION, CLUB, ESCUELA_MUNICIPAL, ORGANIZACION

    @Column(name = "personeria_juridica", length = 100)
    private String personeriaJuridica;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "nro_nit", length = 20)
    private String nroNit;

    // Sports School Specific Fields
    @Column(name = "entrenador", length = 100)
    private String entrenador;

    @Column(name = "disciplina", length = 100)
    private String disciplina;

    @Column(name = "codigo_escuela", length = 50)
    private String codigoEscuela;

    // Payment Configuration
    @Column(name = "requiere_pago_mensualidad", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean requierePagoMensualidad = false;

    @Column(name = "tarifa_mensual")
    private Integer tarifaMensual = 0;

    @Column(name = "permite_reservas_gratuitas", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean permiteReservasGratuitas = false;

    @OneToMany(mappedBy = "organizacion", fetch = FetchType.LAZY)
    private List<OrganizacionRepresentante> representantes = new ArrayList<>();

    public boolean tieneRepresentante(String uuid) {
        return uuid != null && representantes.stream().anyMatch(p ->
                p.getContribuyente() != null && p.getContribuyente().estaActivo()
                        && uuid.equals(p.getContribuyente().getUuid()));
    }

    // Relations
    @OneToMany(mappedBy = "organizacion", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Reserva> reservasList = new ArrayList<>();

    public Organizacion(String uuid) {
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
        if (this.requierePagoMensualidad == null) {
            this.requierePagoMensualidad = false;
        }
        if (this.tarifaMensual == null) {
            this.tarifaMensual = 0;
        }
        if (this.permiteReservasGratuitas == null) {
            this.permiteReservasGratuitas = false;
        }
    }

    // Helper methods
    public boolean estaActiva() {
        return !estado && Boolean.TRUE.equals(activo);
    }

    public boolean puedeRealizarReservasGratuitas() {
        return Boolean.TRUE.equals(permiteReservasGratuitas);
    }

    public boolean requierePagoMensual() {
        return Boolean.TRUE.equals(requierePagoMensualidad) && tarifaMensual != null && tarifaMensual > 0;
    }
}
