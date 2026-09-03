package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.EstudianteEscuelaDto;
import com.gamq.deportes.model.Contribuyente;
import com.gamq.deportes.model.Estudiante;
import com.gamq.deportes.model.EstudianteEscuela;
import com.gamq.deportes.model.Organizacion;

import java.util.stream.Collectors;

public class EstudianteEscuelaMapper {
    public static EstudianteEscuelaDto toDto(EstudianteEscuela inscripcion, boolean includeRelations) {
        if (inscripcion == null) return null;
        EstudianteEscuelaDto dto = new EstudianteEscuelaDto();
        dto.setIdEstudiante(inscripcion.getIdEstudiante());
        dto.setUuid(inscripcion.getUuid());
        dto.setEstado(inscripcion.isEstado());
        dto.setFechaCreacion(inscripcion.getFechaCreacion());
        Estudiante estudiante = inscripcion.getEstudiante();
        if (estudiante != null) {
            dto.setEstudianteUuid(estudiante.getUuid());
            dto.setNombreCompleto(estudiante.getNombreCompleto());
            dto.setFechaNacimiento(estudiante.getFechaNacimiento());
            dto.setGenero(estudiante.getGenero());
            dto.setNumeroDocumento(estudiante.getNumeroDocumento());
            dto.setTelefono(estudiante.getTelefono());
            dto.setEmail(estudiante.getEmail());
        }
        dto.setFechaInscripcion(inscripcion.getFechaInscripcion());
        dto.setFechaBaja(inscripcion.getFechaBaja());
        dto.setMotivoBaja(inscripcion.getMotivoBaja());
        dto.setMatricula(inscripcion.getMatricula());
        dto.setActivo(inscripcion.getActivo());
        if (inscripcion.getContribuyenteDto() != null) {
            dto.setContribuyenteDto(ContribuyenteMapper.toSimpleDto(inscripcion.getContribuyenteDto()));
        }
        if (inscripcion.getOrganizacionDto() != null) {
            dto.setOrganizacionDto(OrganizacionMapper.toDto(inscripcion.getOrganizacionDto(), false));
        }
        if (includeRelations && inscripcion.getMensualidadesDtoList() != null) {
            dto.setMensualidadesDtoList(inscripcion.getMensualidadesDtoList().stream()
                    .map(periodo -> ReservaPeriodoMapper.toDto(periodo, true))
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public static EstudianteEscuela toEntity(EstudianteEscuelaDto dto) {
        if (dto == null) return null;
        EstudianteEscuela inscripcion = new EstudianteEscuela();
        inscripcion.setUuid(dto.getUuid());
        inscripcion.setEstado(dto.isEstado());
        inscripcion.setFechaCreacion(dto.getFechaCreacion());
        inscripcion.setFechaInscripcion(dto.getFechaInscripcion());
        inscripcion.setFechaBaja(dto.getFechaBaja());
        inscripcion.setMotivoBaja(dto.getMotivoBaja());
        inscripcion.setActivo(dto.getActivo());
        inscripcion.setEstudiante(toEstudiante(dto));
        return inscripcion;
    }

    public static Estudiante toEstudiante(EstudianteEscuelaDto dto) {
        Estudiante estudiante = new Estudiante();
        estudiante.setUuid(dto.getEstudianteUuid());
        return updatePersonalDataFromDto(dto, estudiante);
    }

    public static Estudiante updatePersonalDataFromDto(EstudianteEscuelaDto dto, Estudiante estudiante) {
        if (dto == null || estudiante == null) return estudiante;
        if (dto.getNombreCompleto() != null) estudiante.setNombreCompleto(dto.getNombreCompleto());
        estudiante.setFechaNacimiento(dto.getFechaNacimiento());
        estudiante.setGenero(emptyToNull(dto.getGenero()));
        estudiante.setNumeroDocumento(emptyToNull(dto.getNumeroDocumento()));
        estudiante.setTelefono(emptyToNull(dto.getTelefono()));
        estudiante.setEmail(emptyToNull(dto.getEmail()));
        return estudiante;
    }

    public static EstudianteEscuela updateEntityFromDto(EstudianteEscuelaDto dto, EstudianteEscuela inscripcion) {
        if (dto == null || inscripcion == null) return inscripcion;
        Estudiante estudiante = inscripcion.getEstudiante();
        updatePersonalDataFromDto(dto, estudiante);
        if (dto.getFechaInscripcion() != null) inscripcion.setFechaInscripcion(dto.getFechaInscripcion());
        if (dto.getActivo() != null) inscripcion.setActivo(dto.getActivo());
        inscripcion.setMotivoBaja(dto.getMotivoBaja());
        return inscripcion;
    }

    public static EstudianteEscuelaDto toSimpleDto(EstudianteEscuela inscripcion) {
        return toDto(inscripcion, false);
    }

    public static EstudianteEscuela toNewEntity(EstudianteEscuelaDto dto, Contribuyente responsable,
                                                  Organizacion organizacion) {
        EstudianteEscuela inscripcion = toEntity(dto);
        inscripcion.setContribuyenteDto(responsable);
        inscripcion.setOrganizacionDto(organizacion);
        inscripcion.setActivo(true);
        inscripcion.setFechaBaja(null);
        return inscripcion;
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
