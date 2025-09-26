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
        dto.setDimensiones(campo.getDimensiones());
        dto.setCapacidadPersonas(campo.getCapacidadPersonas());
        dto.setTieneIluminacion(campo.getTieneIluminacion());
        dto.setEstadoActual(campo.getEstadoActual());
        dto.setObservaciones(campo.getObservaciones());

        dto.setSuperficieDto(SuperficieMapper.toDto(campo.getSuperficie()));
        if (includeDetails) {
//            if (campo.getComplejo() != null) {
                dto.setComplejoDto(ComplejoDeportivoMapper.toSimpleDto(campo.getComplejo()));
//            }
//            if (campo.getSuperficie() != null) {
//                dto.setSuperficieDto(SuperficieMapper.toDto(campo.getSuperficie()));
//            }
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

//            if (campo.getCategorias() != null) {
//                dto.setCategoriasDtoList(
//                        campo.getCategorias().stream()
//                                .map(CategoriasMapper::toDto)
//                                .map(CategoriasMapper::toDto)
//                                .collect(Collectors.toList())
//                );
//            }
        }
        return dto;
    }
    public static CampoDeportivoDto toSimpleDto(CampoDeportivo campo) {
        return toDto(campo, false);
    }

    public static CampoDeportivo toEntity(CampoDeportivoDto dto) {
        CampoDeportivo campo = new CampoDeportivo();
        return updateEntityFromDto(dto, campo);
    }

    public static CampoDeportivo updateEntityFromDto(CampoDeportivoDto dto, CampoDeportivo campo) {
//    public static CampoDeportivo toEntity(CampoDeportivoDto dto){
//        CampoDeportivo campo =new campoDeportivo();

        campo.setNombre(dto.getNombre());
        campo.setCodigo(dto.getCodigo());
        campo.setDimensiones(dto.getDimensiones());
        campo.setCapacidadPersonas(dto.getCapacidadPersonas());
        campo.setTieneIluminacion(dto.getTieneIluminacion());
        campo.setEstadoActual(dto.getEstadoActual());
        campo.setObservaciones(dto.getObservaciones());

        return campo;
    }
}