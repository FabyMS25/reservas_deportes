package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.EstudianteEscuelaDto;
import com.gamq.deportes.model.Contribuyente;
import com.gamq.deportes.model.EstudianteEscuela;
import com.gamq.deportes.model.Organizacion;

import java.util.stream.Collectors;

public class EstudianteEscuelaMapper {

    public static EstudianteEscuelaDto toDto(EstudianteEscuela estudiante, boolean includeRelations) {
        if (estudiante == null) return null;

        EstudianteEscuelaDto dto = new EstudianteEscuelaDto();

        // Basic identification
        dto.setIdEstudiante(estudiante.getIdEstudiante());
        dto.setUuid(estudiante.getUuid());
        dto.setEstado(estudiante.isEstado());
        dto.setFechaCreacion(estudiante.getFechaCreacion());

        // Student Personal Information
        dto.setNombreCompleto(estudiante.getNombreCompleto());
        dto.setFechaNacimiento(estudiante.getFechaNacimiento());
        dto.setGenero(estudiante.getGenero());
        dto.setNumeroDocumento(estudiante.getNumeroDocumento());
        dto.setTelefono(estudiante.getTelefono());
        dto.setEmail(estudiante.getEmail());

        // School Enrollment Information
        dto.setFechaInscripcion(estudiante.getFechaInscripcion());
        dto.setFechaBaja(estudiante.getFechaBaja());
        dto.setMatricula(estudiante.getMatricula());
        dto.setActivo(estudiante.getActivo() != null ? estudiante.getActivo() : false);

        // Always include the representative/tutor summary. The monthly payment form needs
        // the RUAT codigoContribuyente from the student record to create a valid tasa.
        if (estudiante.getContribuyenteDto() != null) {
            dto.setContribuyenteDto(includeRelations
                    ? ContribuyenteMapper.toDto(estudiante.getContribuyenteDto(), false)
                    : ContribuyenteMapper.toSimpleDto(estudiante.getContribuyenteDto()));
        }

        if (includeRelations) {
            // Include monthly payments (reservas as mensualidades)
            if (estudiante.getMensualidadesDtoList() != null && !estudiante.getMensualidadesDtoList().isEmpty()) {
                dto.setMensualidadesDtoList(estudiante.getMensualidadesDtoList().stream()
                        .map(periodo -> ReservaPeriodoMapper.toDto(periodo, true))
                        .collect(Collectors.toList()));
            }
        }

        return dto;
    }

    public static EstudianteEscuela toEntity(EstudianteEscuelaDto dto) {
        if (dto == null) return null;

        EstudianteEscuela estudiante = new EstudianteEscuela();

        estudiante.setUuid(dto.getUuid());
        estudiante.setEstado(dto.isEstado());
        estudiante.setFechaCreacion(dto.getFechaCreacion());

        // Student Personal Information
        estudiante.setNombreCompleto(dto.getNombreCompleto());
        estudiante.setFechaNacimiento(dto.getFechaNacimiento());
        estudiante.setGenero(dto.getGenero());
        estudiante.setNumeroDocumento(dto.getNumeroDocumento());
        estudiante.setTelefono(dto.getTelefono());
        estudiante.setEmail(dto.getEmail());

        // School Enrollment Information
        estudiante.setFechaInscripcion(dto.getFechaInscripcion());
        estudiante.setFechaBaja(dto.getFechaBaja());
        estudiante.setMatricula(dto.getMatricula());
        estudiante.setActivo(dto.getActivo());

        return estudiante;
    }

    public static EstudianteEscuela updateEntityFromDto(EstudianteEscuelaDto dto, EstudianteEscuela estudiante) {
        if (dto == null || estudiante == null) return estudiante;

        // Update only mutable fields

        // Student Personal Information
        if (dto.getNombreCompleto() != null) {
            estudiante.setNombreCompleto(dto.getNombreCompleto());
        }
        if (dto.getFechaNacimiento() != null) {
            estudiante.setFechaNacimiento(dto.getFechaNacimiento());
        }
        if (dto.getGenero() != null) {
            estudiante.setGenero(dto.getGenero());
        }
        if (dto.getNumeroDocumento() != null) {
            estudiante.setNumeroDocumento(dto.getNumeroDocumento());
        }
        if (dto.getTelefono() != null) {
            estudiante.setTelefono(dto.getTelefono());
        }
        if (dto.getEmail() != null) {
            estudiante.setEmail(dto.getEmail());
        }

        // School Enrollment Information
        if (dto.getFechaInscripcion() != null) {
            estudiante.setFechaInscripcion(dto.getFechaInscripcion());
        }
        if (dto.getFechaBaja() != null) {
            estudiante.setFechaBaja(dto.getFechaBaja());
        }
        if (dto.getMatricula() != null) {
            estudiante.setMatricula(dto.getMatricula());
        }
        // Active is primitive in DTO, so always update
        estudiante.setActivo(dto.getActivo());

        return estudiante;
    }

    // Method to convert to simplified DTO (for dropdowns or basic listings)
    public static EstudianteEscuelaDto toSimpleDto(EstudianteEscuela estudiante) {
        if (estudiante == null) return null;

        EstudianteEscuelaDto dto = new EstudianteEscuelaDto();
        dto.setIdEstudiante(estudiante.getIdEstudiante());
        dto.setUuid(estudiante.getUuid());
        dto.setNombreCompleto(estudiante.getNombreCompleto());
        dto.setMatricula(estudiante.getMatricula());
        dto.setActivo(estudiante.getActivo() != null ? estudiante.getActivo() : false);
        dto.setFechaInscripcion(estudiante.getFechaInscripcion());
        dto.setFechaBaja(estudiante.getFechaBaja());

        // Include basic contribuyente info
        if (estudiante.getContribuyenteDto() != null) {
            dto.setContribuyenteDto(ContribuyenteMapper.toSimpleDto(estudiante.getContribuyenteDto()));
        }

        return dto;
    }

    // Method for creating new student enrollment
    public static EstudianteEscuela toNewEntity(EstudianteEscuelaDto dto, Contribuyente contribuyente, Organizacion organizacion) {
        EstudianteEscuela estudiante = toEntity(dto);
        if (estudiante != null) {
            estudiante.setContribuyenteDto(contribuyente);
//            estudiante.setOrganizacionDto(organizacion);
            estudiante.setActivo(true);
            estudiante.setFechaBaja(null);
        }
        return estudiante;
    }
}
