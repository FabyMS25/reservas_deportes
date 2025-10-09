package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ContribuyenteDto;
import com.gamq.deportes.model.Contribuyente;

import java.util.stream.Collectors;

public class ContribuyenteMapper {
    public static ContribuyenteDto toDto(Contribuyente contribuyente, boolean includeRelations) {
        ContribuyenteDto dto = new ContribuyenteDto();

        // Basic identification
        dto.setUuid(contribuyente.getUuid());
        dto.setEstado(contribuyente.isEstado());
        dto.setFechaCreacion(contribuyente.getFechaCreacion());
        dto.setFechaActualizacion(contribuyente.getFechaActualizacion());

        // Authentication
        dto.setUserName(contribuyente.getUserName());
        dto.setPassword(contribuyente.getPassword());
        dto.setEstadoUsuario(contribuyente.getEstadoUsuario());

        // RUAT Integration
        dto.setCodigoContribuyente(contribuyente.getCodigoContribuyente());
        dto.setTipoContribuyente(contribuyente.getTipoContribuyente());
        dto.setTipoDocumento(contribuyente.getTipoDocumento());
        dto.setNumeroDocumento(contribuyente.getNumeroDocumento());
        dto.setExpedido(contribuyente.getExpedido());
        dto.setNroNit(contribuyente.getNroNit());

        // Personal Information
        dto.setNombre(contribuyente.getNombre());
        dto.setPrimerApellido(contribuyente.getPrimerApellido());
        dto.setSegundoApellido(contribuyente.getSegundoApellido());
        dto.setApellidoEsposo(contribuyente.getApellidoEsposo());
        dto.setEstadoCivil(contribuyente.getEstadoCivil());
        dto.setFechaNacimiento(contribuyente.getFechaNacimiento());
        dto.setGenero(contribuyente.getGenero());

        // Contact Information
        dto.setTelefono(contribuyente.getTelefono());
        dto.setEmail(contribuyente.getEmail());
        dto.setDireccion(contribuyente.getDireccion());

        // RUAT Status
        dto.setEstadoRuat(contribuyente.getEstadoRuat());
        dto.setVerificadoRuat(contribuyente.getVerificadoRuat());
        dto.setFechaVerificacionRuat(contribuyente.getFechaVerificacionRuat());

        // Special Categories and Benefits
        dto.setEsDeportistaElite(contribuyente.getEsDeportistaElite());
        dto.setPerteneceEscuelaMunicipal(contribuyente.getPerteneceEscuelaMunicipal());
        dto.setRegistroUnicoDeportivo(contribuyente.getRegistroUnicoDeportivo());
        dto.setFechaUltimoUsoGratuito(contribuyente.getFechaUltimoUsoGratuito());
        dto.setObservacionesMunicipales(contribuyente.getObservacionesMunicipales());
        dto.setDescuentoAplicable(contribuyente.getDescuentoAplicable());

        // Sanctions
        dto.setEstaSancionado(contribuyente.getEstaSancionado());
        dto.setFechaFinSancion(contribuyente.getFechaFinSancion());
        dto.setMotivoSancion(contribuyente.getMotivoSancion());

        dto.setNombreCompleto(contribuyente.getNombreCompleto());
        dto.setPuedeReservarGratis(contribuyente.puedeReservarGratis());
        dto.setEstaActivo(contribuyente.estaActivo());
        if (includeRelations) {
            // Include organizations represented
            if (contribuyente.getOrganizacionesRepresentadas() != null && !contribuyente.getOrganizacionesRepresentadas().isEmpty()) {
                dto.setOrganizacionesRepresentadas(contribuyente.getOrganizacionesRepresentadas().stream()
                        .map(organizacion -> OrganizacionMapper.toDto(organizacion, false))
                        .collect(Collectors.toList()));
            }

            // Include reservations if needed
            if (contribuyente.getReservasList() != null && !contribuyente.getReservasList().isEmpty()) {
                dto.setReservasList(contribuyente.getReservasList().stream()
                        .map(reserva -> ReservaMapper.toDto(reserva)) // Assuming you have ReservaMapper
                        .collect(Collectors.toList()));
            }
        }
        return dto;
    }

    public static Contribuyente toEntity(ContribuyenteDto dto) {
//        if (dto == null) return null;

        Contribuyente contribuyente = new Contribuyente();

        contribuyente.setUuid(dto.getUuid());
        contribuyente.setEstado(dto.isEstado());
        contribuyente.setFechaCreacion(dto.getFechaCreacion());
        contribuyente.setFechaActualizacion(dto.getFechaActualizacion());

        // Authentication
        contribuyente.setUserName(dto.getUserName());
        contribuyente.setPassword(dto.getPassword());
        contribuyente.setEstadoUsuario(dto.getEstadoUsuario());

        // RUAT Integration
        contribuyente.setCodigoContribuyente(dto.getCodigoContribuyente());
        contribuyente.setTipoContribuyente(dto.getTipoContribuyente());
        contribuyente.setTipoDocumento(dto.getTipoDocumento());
        contribuyente.setNumeroDocumento(dto.getNumeroDocumento());
        contribuyente.setExpedido(dto.getExpedido());
        contribuyente.setNroNit(dto.getNroNit());

        // Personal Information
        contribuyente.setNombre(dto.getNombre());
        contribuyente.setPrimerApellido(dto.getPrimerApellido());
        contribuyente.setSegundoApellido(dto.getSegundoApellido());
        contribuyente.setApellidoEsposo(dto.getApellidoEsposo());
        contribuyente.setEstadoCivil(dto.getEstadoCivil());
        contribuyente.setFechaNacimiento(dto.getFechaNacimiento());
        contribuyente.setGenero(dto.getGenero());

        // Contact Information
        contribuyente.setTelefono(dto.getTelefono());
        contribuyente.setEmail(dto.getEmail());
        contribuyente.setDireccion(dto.getDireccion());

        // RUAT Status
        contribuyente.setEstadoRuat(dto.getEstadoRuat());
        contribuyente.setVerificadoRuat(dto.getVerificadoRuat());
        contribuyente.setFechaVerificacionRuat(dto.getFechaVerificacionRuat());

        // Special Categories and Benefits
        contribuyente.setEsDeportistaElite(dto.getEsDeportistaElite());
        contribuyente.setPerteneceEscuelaMunicipal(dto.getPerteneceEscuelaMunicipal());
        contribuyente.setRegistroUnicoDeportivo(dto.getRegistroUnicoDeportivo());
        contribuyente.setFechaUltimoUsoGratuito(dto.getFechaUltimoUsoGratuito());
        contribuyente.setObservacionesMunicipales(dto.getObservacionesMunicipales());
        contribuyente.setDescuentoAplicable(dto.getDescuentoAplicable());

        // Sanctions
        contribuyente.setEstaSancionado(dto.getEstaSancionado());
        contribuyente.setFechaFinSancion(dto.getFechaFinSancion());
        contribuyente.setMotivoSancion(dto.getMotivoSancion());

        return contribuyente;
    }

    public static Contribuyente updateEntityFromDto(ContribuyenteDto dto, Contribuyente existing) {
        if (dto == null || existing == null) {
            return existing;
        }

        // Update only non-null fields from DTO
        if (dto.getUserName() != null) {
            existing.setUserName(dto.getUserName());
        }
        if (dto.getPassword() != null) {
            existing.setPassword(dto.getPassword());
        }
        if (dto.getEstadoUsuario() != null) {
            existing.setEstadoUsuario(dto.getEstadoUsuario());
        }

        // Personal Information
        if (dto.getNombre() != null) {
            existing.setNombre(dto.getNombre());
        }
        if (dto.getPrimerApellido() != null) {
            existing.setPrimerApellido(dto.getPrimerApellido());
        }
        if (dto.getSegundoApellido() != null) {
            existing.setSegundoApellido(dto.getSegundoApellido());
        }
        if (dto.getApellidoEsposo() != null) {
            existing.setApellidoEsposo(dto.getApellidoEsposo());
        }
        if (dto.getEstadoCivil() != null) {
            existing.setEstadoCivil(dto.getEstadoCivil());
        }
        if (dto.getFechaNacimiento() != null) {
            existing.setFechaNacimiento(dto.getFechaNacimiento());
        }
        if (dto.getGenero() != null) {
            existing.setGenero(dto.getGenero());
        }

        // Contact Information
        if (dto.getTelefono() != null) {
            existing.setTelefono(dto.getTelefono());
        }
        if (dto.getEmail() != null) {
            existing.setEmail(dto.getEmail());
        }
        if (dto.getDireccion() != null) {
            existing.setDireccion(dto.getDireccion());
        }

        // Special Categories (Boolean fields can be explicitly set to null)
        if (dto.getEsDeportistaElite() != null) {
            existing.setEsDeportistaElite(dto.getEsDeportistaElite());
        }
        if (dto.getPerteneceEscuelaMunicipal() != null) {
            existing.setPerteneceEscuelaMunicipal(dto.getPerteneceEscuelaMunicipal());
        }
        if (dto.getRegistroUnicoDeportivo() != null) {
            existing.setRegistroUnicoDeportivo(dto.getRegistroUnicoDeportivo());
        }
        if (dto.getFechaUltimoUsoGratuito() != null) {
            existing.setFechaUltimoUsoGratuito(dto.getFechaUltimoUsoGratuito());
        }
        if (dto.getObservacionesMunicipales() != null) {
            existing.setObservacionesMunicipales(dto.getObservacionesMunicipales());
        }
        if (dto.getDescuentoAplicable() != null) {
            existing.setDescuentoAplicable(dto.getDescuentoAplicable());
        }

        // Sanctions
        if (dto.getEstaSancionado() != null) {
            existing.setEstaSancionado(dto.getEstaSancionado());
        }
        if (dto.getFechaFinSancion() != null) {
            existing.setFechaFinSancion(dto.getFechaFinSancion());
        }
        if (dto.getMotivoSancion() != null) {
            existing.setMotivoSancion(dto.getMotivoSancion());
        }

        return existing;
    }

    // Method to convert to simplified DTO (for dropdowns or basic listings)
    public static ContribuyenteDto toSimpleDto(Contribuyente contribuyente) {
//        if (contribuyente == null) {return null;}

        ContribuyenteDto dto = new ContribuyenteDto();
        dto.setUuid(contribuyente.getUuid());
        dto.setNombre(contribuyente.getNombre());
        dto.setPrimerApellido(contribuyente.getPrimerApellido());
        dto.setSegundoApellido(contribuyente.getSegundoApellido());
        dto.setEmail(contribuyente.getEmail());
        dto.setTelefono(contribuyente.getTelefono());
        dto.setNombreCompleto(contribuyente.getNombreCompleto());
        dto.setNumeroDocumento(contribuyente.getNumeroDocumento());
        dto.setEstadoUsuario(contribuyente.getEstadoUsuario());
//        dto.setNombreCompleto(contribuyente.getNombreCompleto());
//        dto.setEstaActivo(contribuyente.estaActivo());

        return dto;
    }
}