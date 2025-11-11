package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.CampoDeportivoDto;
import com.gamq.deportes.model.CampoDeportivo;

import java.util.stream.Collectors;

public class CampoDeportivoMapper {

    public static CampoDeportivoDto toDto(CampoDeportivo campo, boolean includeDetails) {
        CampoDeportivoDto dto = new CampoDeportivoDto();

        dto.setUuid(campo.getUuid());
        dto.setEstado(campo.isEstado());
        dto.setFechaCreacion(campo.getFechaCreacion());

        dto.setNombre(campo.getNombre());
        dto.setCodigo(campo.getCodigo());
        dto.setCategoria(campo.getCategoria());
        dto.setDimensiones(campo.getDimensiones());
        dto.setCapacidadPersonas(campo.getCapacidadPersonas());
        dto.setTieneIluminacion(campo.getTieneIluminacion());
        dto.setEstadoActual(campo.getEstadoActual());
        dto.setObservaciones(campo.getObservaciones());
        dto.setAccesoLibre(campo.getAccesoLibre());
        dto.setRequiereAprobacion(campo.getRequiereAprobacion());
        dto.setHorasMinimaAprobacion(campo.getHorasMinimaAprobacion());
        dto.setMaxDuracionAutoAprobacion(campo.getMaxDuracionAutoAprobacion());
        dto.setMaxPersonasAutoAprobacion(campo.getMaxPersonasAutoAprobacion());

        dto.setSuperficieDto(SuperficieMapper.toDto(campo.getSuperficie()));
        if (includeDetails) {
            dto.setComplejoDto(ComplejoDeportivoMapper.toSimpleDto(campo.getComplejo()));
        }
            if (campo.getTarifasList() != null) {
                dto.setTarifasDtoList(
                        campo.getTarifasList().stream()
                                .map(tarifa -> TarifaMapper.toDto(tarifa, false))
                                .collect(Collectors.toList())
                );
            }
            if (campo.getHorariosFijosList() != null) {
                dto.setHorariosFijosDtoList(
                        campo.getHorariosFijosList().stream()
                                .map(horario -> HorarioFijoMapper.toDto(horario, false))
                                .collect(Collectors.toList())
                );
            }
            if (campo.getHorariosExcepcionesList() != null) {
                dto.setHorariosExcepcionesDtoList(
                        campo.getHorariosExcepcionesList().stream()
                                .map(horario -> HorarioExcepcionMapper.toDto(horario, false))
                                .collect(Collectors.toList())
                );
            }
        return dto;
    }
    public static CampoDeportivoDto toSimpleDto(CampoDeportivo campo) {
        return toDto(campo, false);
    }

    public static CampoDeportivo toEntity(CampoDeportivoDto dto) {
        CampoDeportivo campo = new CampoDeportivo();
        campo.setUuid(dto.getUuid());
        campo.setEstado(dto.isEstado());
        campo.setFechaCreacion(dto.getFechaCreacion());
        return updateEntityFromDto(dto, campo);
    }

    public static CampoDeportivo updateEntityFromDto(CampoDeportivoDto dto, CampoDeportivo campo) {
        campo.setNombre(dto.getNombre());
        campo.setCodigo(dto.getCodigo());
        campo.setCategoria(dto.getCategoria());
        campo.setDimensiones(dto.getDimensiones());
        campo.setCapacidadPersonas(dto.getCapacidadPersonas());
        campo.setTieneIluminacion(dto.getTieneIluminacion());
        campo.setEstadoActual(dto.getEstadoActual());
        campo.setObservaciones(dto.getObservaciones());
        campo.setAccesoLibre(dto.getAccesoLibre());
        campo.setRequiereAprobacion(dto.getRequiereAprobacion());
        campo.setHorasMinimaAprobacion(dto.getHorasMinimaAprobacion());
        campo.setMaxDuracionAutoAprobacion(dto.getMaxDuracionAutoAprobacion());
        campo.setMaxPersonasAutoAprobacion(dto.getMaxPersonasAutoAprobacion());

        return campo;
    }
}