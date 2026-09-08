package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gamq.deportes.model.Organizacion;
import com.gamq.deportes.model.Reserva;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContribuyenteDto {
    private Long idContribuyente;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private String userName;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String password;
    private String estadoUsuario;

    // RUAT Integration
    private String estadoRuat; // ACTIVO, BAJA from RUAT
    private Boolean verificadoRuat = false;
    private LocalDateTime fechaVerificacionRuat;
    private String tipoContribuyente; // NA=Natural, JU=Juridica
    private String codigoContribuyente;
    private String tipoDocumento; // CI, CE
    private String numeroDocumento;
    private String expedido; // Department code 1-9

    private String nroNit;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String apellidoEsposo;
    private LocalDate fechaNacimiento;
    private String estadoCivil; // SO, CA, VI, DI
    private String genero; // M, F

    private String telefono;
    private String email;
    private String direccion;

    private Boolean esDeportistaElite = false;
    private String registroUnicoDeportivo;
//    private Double descuentoAplicable = 0.0;
    private LocalDate fechaUltimoUsoGratuito;
    private String observacionesMunicipales;

    private Boolean estaSancionado = false;
    private LocalDateTime fechaFinSancion;
    private String motivoSancion;

    private String nombreCompleto;
    private boolean puedeReservarGratis;
    private Boolean estaActivo;

    // Address (from RUAT datosDireccion)
//    private String area;
//    private String distrito;
//    private String tipoLugar;
//    private String nombreLugar;
//    private String numeroPuerta;
//    private String direccionDescriptiva;
//    private String nombreEdificio;
//    private String bloque;
//    private String piso;
//    private String departamento;

    private List<ReservaDto> reservasList = new ArrayList<>();
    private List<OrganizacionRepresentanteDto> organizacionesVinculadas;
    private List<OrganizacionDto> organizacionesRepresentadas;
    private List<EstudianteEscuelaDto> estudianteEnEscuelas = new ArrayList<>();

}
