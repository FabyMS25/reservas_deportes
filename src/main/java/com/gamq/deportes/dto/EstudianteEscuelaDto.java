package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

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
public class EstudianteEscuelaDto {
    private Long idEstudiante;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private String genero; // M, F
    private String numeroDocumento;
    private String telefono;
    private String email;

    private LocalDate fechaInscripcion;
    private LocalDate fechaBaja;
    private String matricula;
    private Boolean activo;

    private ContribuyenteDto contribuyenteDto; // representante o tutor
//    organizacionDto: Organizacion;
    private List<ReservaPeriodoDto> mensualidadesDtoList = new ArrayList<>();
//    private List<OrganizacionDto> organizacionesRepresentadas = new ArrayList<>();
}
