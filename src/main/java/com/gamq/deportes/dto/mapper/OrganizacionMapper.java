package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.OrganizacionDto;
import com.gamq.deportes.model.Organizacion;
import java.util.stream.Collectors;

public class OrganizacionMapper {

    public static OrganizacionDto toDto(Organizacion organizacion, boolean includeRelations) {
        if (organizacion == null) return null;

        OrganizacionDto dto = new OrganizacionDto();

        // Basic identification
        dto.setIdOrganizacion(organizacion.getIdOrganizacion());
        dto.setUuid(organizacion.getUuid());
        dto.setEstado(organizacion.isEstado());
        dto.setFechaCreacion(organizacion.getFechaCreacion());

        // Organization status
        dto.setActivo(organizacion.getActivo());

        // Organization Type and Legal Information
        dto.setTipoOrganizacion(organizacion.getTipoOrganizacion());
        dto.setPersoneriaJuridica(organizacion.getPersoneriaJuridica());
        dto.setNombre(organizacion.getNombre());
        dto.setTelefono(organizacion.getTelefono());
        dto.setEmail(organizacion.getEmail());
        dto.setDireccion(organizacion.getDireccion());
        dto.setNroNit(organizacion.getNroNit());

        // Sports School Specific Fields
        dto.setEntrenador(organizacion.getEntrenador());
        dto.setDisciplina(organizacion.getDisciplina());
        dto.setCodigoEscuela(organizacion.getCodigoEscuela());

        // Payment Configuration
        dto.setRequierePagoMensualidad(organizacion.getRequierePagoMensualidad());
        dto.setTarifaMensual(organizacion.getTarifaMensual() != null ? organizacion.getTarifaMensual() : 0);
        dto.setPermiteReservasGratuitas(organizacion.getPermiteReservasGratuitas());

        dto.setRepresentantesDto(organizacion.getRepresentantes().stream()
            .filter(p -> p.getContribuyente() != null)
            .map(p -> new com.gamq.deportes.dto.OrganizacionRepresentanteDto()
                .setContribuyenteDto(includeRelations
                        ? ContribuyenteMapper.toDto(p.getContribuyente(), false)
                        : ContribuyenteMapper.toSimpleDto(p.getContribuyente())))
            .collect(Collectors.toList()));
        if (includeRelations) {
            // Include reservations
            if (organizacion.getReservasList() != null && !organizacion.getReservasList().isEmpty()) {
                dto.setReservasDtoList(organizacion.getReservasList().stream()
                        .map(ReservaMapper::toDto)
                        .collect(Collectors.toList()));
            }
        }

        return dto;
    }

    public static Organizacion toEntity(OrganizacionDto dto) {
        if (dto == null) return null;

        Organizacion organizacion = new Organizacion();

        organizacion.setUuid(dto.getUuid());
        organizacion.setEstado(dto.isEstado());
        organizacion.setFechaCreacion(dto.getFechaCreacion());

        // Organization status
        organizacion.setActivo(dto.getActivo());

        // Organization Type and Legal Information
        organizacion.setTipoOrganizacion(dto.getTipoOrganizacion());
        organizacion.setPersoneriaJuridica(dto.getPersoneriaJuridica());
        organizacion.setNombre(dto.getNombre());
        organizacion.setTelefono(dto.getTelefono());
        organizacion.setEmail(dto.getEmail());
        organizacion.setDireccion(dto.getDireccion());
        organizacion.setNroNit(dto.getNroNit());

        // Sports School Specific Fields
        organizacion.setEntrenador(dto.getEntrenador());
        organizacion.setDisciplina(dto.getDisciplina());
        organizacion.setCodigoEscuela(dto.getCodigoEscuela());

        // Payment Configuration
        organizacion.setRequierePagoMensualidad(dto.getRequierePagoMensualidad());
        organizacion.setTarifaMensual(dto.getTarifaMensual());
        organizacion.setPermiteReservasGratuitas(dto.getPermiteReservasGratuitas());

        return organizacion;
    }

    public static Organizacion updateEntityFromDto(OrganizacionDto dto, Organizacion organizacion) {
        if (dto == null || organizacion == null) return organizacion;

        // Update only mutable fields

        // Organization status
        if (dto.getActivo() != null) {
            organizacion.setActivo(dto.getActivo());
        }

        // Organization Type and Legal Information
        if (dto.getTipoOrganizacion() != null) {
            organizacion.setTipoOrganizacion(dto.getTipoOrganizacion());
        }
        if (dto.getPersoneriaJuridica() != null) {
            organizacion.setPersoneriaJuridica(dto.getPersoneriaJuridica());
        }
        if (dto.getNombre() != null) {
            organizacion.setNombre(dto.getNombre());
        }
        if (dto.getTelefono() != null) {
            organizacion.setTelefono(dto.getTelefono());
        }
        if (dto.getEmail() != null) {
            organizacion.setEmail(dto.getEmail());
        }
        if (dto.getDireccion() != null) {
            organizacion.setDireccion(dto.getDireccion());
        }
        if (dto.getNroNit() != null) {
            organizacion.setNroNit(dto.getNroNit());
        }

        // Sports School Specific Fields
        if (dto.getEntrenador() != null) {
            organizacion.setEntrenador(dto.getEntrenador());
        }
        if (dto.getDisciplina() != null) {
            organizacion.setDisciplina(dto.getDisciplina());
        }
        if (dto.getCodigoEscuela() != null) {
            organizacion.setCodigoEscuela(dto.getCodigoEscuela());
        }

        // Payment Configuration - these are primitive booleans in DTO, so always update
        organizacion.setRequierePagoMensualidad(dto.getRequierePagoMensualidad());
        organizacion.setTarifaMensual(dto.getTarifaMensual());
        organizacion.setPermiteReservasGratuitas(dto.getPermiteReservasGratuitas());

        return organizacion;
    }

    // Method to convert to simplified DTO (for dropdowns or basic listings)
    public static OrganizacionDto toSimpleDto(Organizacion organizacion) {
        if (organizacion == null) return null;

        OrganizacionDto dto = new OrganizacionDto();
        dto.setIdOrganizacion(organizacion.getIdOrganizacion());
        dto.setUuid(organizacion.getUuid());
        dto.setNombre(organizacion.getNombre());
        dto.setTipoOrganizacion(organizacion.getTipoOrganizacion());
        dto.setCodigoEscuela(organizacion.getCodigoEscuela());
        dto.setActivo(organizacion.getActivo());
        dto.setDisciplina(organizacion.getDisciplina());

        return dto;
    }
}
