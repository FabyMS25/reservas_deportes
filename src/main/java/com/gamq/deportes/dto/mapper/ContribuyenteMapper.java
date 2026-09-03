package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ContribuyenteDto;
import com.gamq.deportes.model.Contribuyente;
import com.gamq.deportes.model.Organizacion;

import java.util.UUID;
import java.util.stream.Collectors;

public class ContribuyenteMapper {
    public static ContribuyenteDto toDto(Contribuyente contribuyente, boolean includeRelations) {
        ContribuyenteDto dto = new ContribuyenteDto();

        dto.setUuid(contribuyente.getUuid());
        dto.setEstado(contribuyente.isEstado());
        dto.setFechaCreacion(contribuyente.getFechaCreacion());
        dto.setFechaActualizacion(contribuyente.getFechaActualizacion());

        // Authentication
        dto.setUserName(contribuyente.getUserName());
        dto.setEstadoUsuario(contribuyente.getEstadoUsuario());

        // RUAT Integration
        dto.setEstadoRuat(contribuyente.getEstadoRuat());
        dto.setVerificadoRuat(contribuyente.getVerificadoRuat());
        dto.setFechaVerificacionRuat(contribuyente.getFechaVerificacionRuat());
        dto.setTipoContribuyente(contribuyente.getTipoContribuyente());
        dto.setCodigoContribuyente(contribuyente.getCodigoContribuyente());
        dto.setTipoDocumento(contribuyente.getTipoDocumento());
        dto.setNumeroDocumento(contribuyente.getNumeroDocumento());
        dto.setExpedido(contribuyente.getExpedido());
        dto.setNroNit(contribuyente.getNroNit());

        // Personal Information
        dto.setNombre(contribuyente.getNombre());
        dto.setPrimerApellido(contribuyente.getPrimerApellido());
        dto.setSegundoApellido(contribuyente.getSegundoApellido());
        dto.setApellidoEsposo(contribuyente.getApellidoEsposo());
        dto.setFechaNacimiento(contribuyente.getFechaNacimiento());
        dto.setEstadoCivil(contribuyente.getEstadoCivil());
        dto.setGenero(contribuyente.getGenero());

        dto.setTelefono(contribuyente.getTelefono());
        dto.setEmail(contribuyente.getEmail());
        dto.setDireccion(contribuyente.getDireccion());

        // Special Categories and Benefits
        dto.setEsDeportistaElite(contribuyente.getEsDeportistaElite());
        dto.setRegistroUnicoDeportivo(contribuyente.getRegistroUnicoDeportivo());
        dto.setFechaUltimoUsoGratuito(contribuyente.getFechaUltimoUsoGratuito());
        dto.setObservacionesMunicipales(contribuyente.getObservacionesMunicipales());

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

            if (contribuyente.getEstudianteEnEscuelas() != null && !contribuyente.getEstudianteEnEscuelas().isEmpty()) {
                dto.setEstudianteEnEscuelas(contribuyente.getEstudianteEnEscuelas().stream()
                        .map(estudiante -> EstudianteEscuelaMapper.toDto(estudiante, false))
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
        Contribuyente contribuyente = new Contribuyente();

        // Only set UUID if it's provided and valid, otherwise let @PrePersist generate it
        if (dto.getUuid() != null && !dto.getUuid().trim().isEmpty()) {
            contribuyente.setUuid(dto.getUuid());
        }

        contribuyente.setEstado(dto.isEstado());
        contribuyente.setFechaCreacion(dto.getFechaCreacion());
        contribuyente.setFechaActualizacion(dto.getFechaActualizacion());

        // Authentication
        contribuyente.setUserName(dto.getUserName());
        contribuyente.setPassword(dto.getPassword());
        contribuyente.setEstadoUsuario(dto.getEstadoUsuario());

        // RUAT Integration
        contribuyente.setEstadoRuat(dto.getEstadoRuat());
        contribuyente.setVerificadoRuat(dto.getVerificadoRuat());
        contribuyente.setFechaVerificacionRuat(dto.getFechaVerificacionRuat());
        contribuyente.setTipoContribuyente(dto.getTipoContribuyente());
        contribuyente.setCodigoContribuyente(dto.getCodigoContribuyente());
        contribuyente.setTipoDocumento(dto.getTipoDocumento());
        contribuyente.setNumeroDocumento(dto.getNumeroDocumento());
        contribuyente.setExpedido(dto.getExpedido());
        contribuyente.setNroNit(dto.getNroNit());

        // Personal Information
        contribuyente.setNombre(dto.getNombre());
        contribuyente.setPrimerApellido(dto.getPrimerApellido());
        contribuyente.setSegundoApellido(dto.getSegundoApellido());
        contribuyente.setApellidoEsposo(dto.getApellidoEsposo());
        contribuyente.setFechaNacimiento(dto.getFechaNacimiento());
        contribuyente.setEstadoCivil(dto.getEstadoCivil());
        contribuyente.setGenero(dto.getGenero());

        contribuyente.setTelefono(dto.getTelefono());
        contribuyente.setEmail(dto.getEmail());
        contribuyente.setDireccion(dto.getDireccion());

        // Special Categories and Benefits
        contribuyente.setEsDeportistaElite(dto.getEsDeportistaElite());
        contribuyente.setRegistroUnicoDeportivo(dto.getRegistroUnicoDeportivo());
        contribuyente.setFechaUltimoUsoGratuito(dto.getFechaUltimoUsoGratuito());
        contribuyente.setObservacionesMunicipales(dto.getObservacionesMunicipales());

        // Sanctions
        contribuyente.setEstaSancionado(dto.getEstaSancionado());
        contribuyente.setFechaFinSancion(dto.getFechaFinSancion());
        contribuyente.setMotivoSancion(dto.getMotivoSancion());

        // Handle related entities - Generate UUIDs if null
        if (dto.getOrganizacionesRepresentadas() != null) {
            contribuyente.setOrganizacionesRepresentadas(
                    dto.getOrganizacionesRepresentadas().stream()
                            .map(orgDto -> {
                                Organizacion organizacion = OrganizacionMapper.toEntity(orgDto);
                                // Generate UUID if null
                                if (organizacion.getUuid() == null || organizacion.getUuid().trim().isEmpty()) {
                                    organizacion.setUuid(UUID.randomUUID().toString());
                                }
                                // Set the relationship
                                organizacion.setRepresentante(contribuyente);
                                return organizacion;
                            })
                            .collect(Collectors.toList())
            );
        }

        return contribuyente;
    }
    public static Contribuyente updateEntityFromDto(ContribuyenteDto dto, Contribuyente existing) {
        if (dto == null || existing == null) {
            return existing;
        }

        // Update only non-null fields from DTO

        // Authentication
        if (dto.getUserName() != null) {
            existing.setUserName(dto.getUserName());
        }
        if (dto.getPassword() != null) {
            existing.setPassword(dto.getPassword());
        }
        if (dto.getEstadoUsuario() != null) {
            existing.setEstadoUsuario(dto.getEstadoUsuario());
        }

        // RUAT Integration
        if (dto.getEstadoRuat() != null) {
            existing.setEstadoRuat(dto.getEstadoRuat());
        }
        if (dto.getVerificadoRuat() != null) {
            existing.setVerificadoRuat(dto.getVerificadoRuat());
        }
        if (dto.getFechaVerificacionRuat() != null) {
            existing.setFechaVerificacionRuat(dto.getFechaVerificacionRuat());
        }
        if (dto.getTipoContribuyente() != null) {
            existing.setTipoContribuyente(dto.getTipoContribuyente());
        }
        if (dto.getCodigoContribuyente() != null) {
            existing.setCodigoContribuyente(dto.getCodigoContribuyente());
        }
        if (dto.getTipoDocumento() != null) {
            existing.setTipoDocumento(dto.getTipoDocumento());
        }
        if (dto.getNumeroDocumento() != null) {
            existing.setNumeroDocumento(dto.getNumeroDocumento());
        }
        if (dto.getExpedido() != null) {
            existing.setExpedido(dto.getExpedido());
        }
        if (dto.getNroNit() != null) {
            existing.setNroNit(dto.getNroNit());
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
        if (dto.getFechaNacimiento() != null) {
            existing.setFechaNacimiento(dto.getFechaNacimiento());
        }
        if (dto.getEstadoCivil() != null) {
            existing.setEstadoCivil(dto.getEstadoCivil());
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

        // Special Categories and Benefits
        if (dto.getEsDeportistaElite() != null) {
            existing.setEsDeportistaElite(dto.getEsDeportistaElite());
        }
        if (dto.getRegistroUnicoDeportivo() !=null){
            existing.setRegistroUnicoDeportivo(dto.getRegistroUnicoDeportivo());
        }
        if (dto.getFechaUltimoUsoGratuito() != null) {
            existing.setFechaUltimoUsoGratuito(dto.getFechaUltimoUsoGratuito());
        }
        if (dto.getObservacionesMunicipales() != null) {
            existing.setObservacionesMunicipales(dto.getObservacionesMunicipales());
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

    public static ContribuyenteDto toSimpleDto(Contribuyente contribuyente) {
        ContribuyenteDto dto = new ContribuyenteDto();

        dto.setIdContribuyente(contribuyente.getIdContribuyente());
        dto.setUuid(contribuyente.getUuid());
        dto.setNombre(contribuyente.getNombre());
        dto.setPrimerApellido(contribuyente.getPrimerApellido());
        dto.setSegundoApellido(contribuyente.getSegundoApellido());
        dto.setEmail(contribuyente.getEmail());
        dto.setTelefono(contribuyente.getTelefono());
        dto.setNumeroDocumento(contribuyente.getNumeroDocumento());
        dto.setCodigoContribuyente(contribuyente.getCodigoContribuyente());
        dto.setTipoContribuyente(contribuyente.getTipoContribuyente());
        dto.setEstadoRuat(contribuyente.getEstadoRuat());
        dto.setVerificadoRuat(contribuyente.getVerificadoRuat());
        dto.setEstadoUsuario(contribuyente.getEstadoUsuario());
        dto.setNombreCompleto(contribuyente.getNombreCompleto());
        dto.setEstaActivo(contribuyente.estaActivo());

        return dto;
    }
}
