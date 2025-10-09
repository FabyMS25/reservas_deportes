package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.OrganizacionDto;
import com.gamq.deportes.model.Organizacion;
import java.util.stream.Collectors;

public class OrganizacionMapper {

    public static OrganizacionDto toDto(Organizacion organizacion, boolean includeRelations) {
        if (organizacion == null) return null;

        OrganizacionDto dto = new OrganizacionDto();
        dto.setUuid(organizacion.getUuid());
        dto.setEstado(organizacion.isEstado());
        dto.setFechaCreacion(organizacion.getFechaCreacion());

        // Core organization fields
        dto.setTipoOrganizacion(organizacion.getTipoOrganizacion());
        dto.setNombre(organizacion.getNombre());
        dto.setTelefono(organizacion.getTelefono());
        dto.setEmail(organizacion.getEmail());
        dto.setDireccion(organizacion.getDireccion());
        dto.setPersoneriaJuridica(organizacion.getPersoneriaJuridica());
        dto.setNroNit(organizacion.getNroNit());
        dto.setActivo(organizacion.getActivo());

        if (includeRelations) {
            // Include representante (contribuyente)
            if (organizacion.getRepresentante() != null) {
                dto.setRepresentanteDto(ContribuyenteMapper.toDto(organizacion.getRepresentante(), false));
            }

            // Include reservations if needed (be careful with circular references)
//            if (organizacion.getReservasList() != null && !organizacion.getReservasList().isEmpty()) {
//                dto.setReservasDtoList(organizacion.getReservasList().stream()
//                        .map(reserva -> ReservaMapper.toDto(reserva)) // Assuming you have ReservaMapper
//                        .collect(Collectors.toList()));
//            }
        }

        return dto;
    }

    public static Organizacion toEntity(OrganizacionDto dto) {
        if (dto == null) return null;

        Organizacion organizacion = new Organizacion();
        organizacion.setUuid(dto.getUuid());
        organizacion.setEstado(dto.isEstado());
        organizacion.setFechaCreacion(dto.getFechaCreacion());

        // Core organization fields
        organizacion.setTipoOrganizacion(dto.getTipoOrganizacion());
        organizacion.setNombre(dto.getNombre());
        organizacion.setTelefono(dto.getTelefono());
        organizacion.setEmail(dto.getEmail());
        organizacion.setDireccion(dto.getDireccion());
        organizacion.setPersoneriaJuridica(dto.getPersoneriaJuridica());
        organizacion.setNroNit(dto.getNroNit());
        organizacion.setActivo(dto.getActivo());

        return organizacion;
    }

    public static Organizacion updateEntityFromDto(OrganizacionDto dto, Organizacion organizacion) {
        if (dto == null || organizacion == null) return organizacion;

        // Update only mutable fields
        organizacion.setTipoOrganizacion(dto.getTipoOrganizacion());
        organizacion.setNombre(dto.getNombre());
        organizacion.setTelefono(dto.getTelefono());
        organizacion.setEmail(dto.getEmail());
        organizacion.setDireccion(dto.getDireccion());
        organizacion.setPersoneriaJuridica(dto.getPersoneriaJuridica());
        organizacion.setNroNit(dto.getNroNit());
        organizacion.setActivo(dto.getActivo());

        return organizacion;
    }
}