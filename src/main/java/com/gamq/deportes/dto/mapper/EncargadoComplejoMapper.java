package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.EncargadoComplejoDto;
import com.gamq.deportes.model.EncargadoComplejo;

public class EncargadoComplejoMapper {
    public static EncargadoComplejoDto toDto(EncargadoComplejo encargado, boolean includeComplejo) {
        EncargadoComplejoDto dto = new EncargadoComplejoDto();

        dto.setUuid(encargado.getUuid());
        dto.setEstado(encargado.isEstado());
        dto.setFechaCreacion(encargado.getFechaCreacion());

        dto.setUserUuid(encargado.getUserUuid());
        dto.setNombre(encargado.getNombre());
        dto.setAvatar(encargado.getAvatar());
        dto.setTelefono(encargado.getTelefono());
        dto.setActivo(encargado.getActivo());
        dto.setFechaAsignacion(encargado.getFechaAsignacion());
        dto.setFechaFinAsignacion(encargado.getFechaFinAsignacion());
        dto.setHoraEntrada(encargado.getHoraEntrada());
        dto.setHoraSalida(encargado.getHoraSalida());

        if (includeComplejo) {
            dto.setComplejoDto(ComplejoDeportivoMapper.toSimpleDto(encargado.getComplejo()));
        }
        return dto;
    }
    public static EncargadoComplejo toEntity(EncargadoComplejoDto dto) {
        EncargadoComplejo encargado = new EncargadoComplejo();
        return updateEntityFromDto(dto, encargado);
    }

    public static EncargadoComplejo updateEntityFromDto(EncargadoComplejoDto dto, EncargadoComplejo encargado) {
//        encargado.setUuid(dto.getUuid());
//        encargado.setEstado(dto.isEstado());
        encargado.setUserUuid(dto.getUserUuid());
        encargado.setNombre(dto.getNombre());
        encargado.setAvatar(dto.getAvatar());
        encargado.setTelefono(dto.getTelefono());
        encargado.setActivo(dto.getActivo());
        encargado.setFechaAsignacion(dto.getFechaAsignacion());
        encargado.setFechaFinAsignacion(dto.getFechaFinAsignacion());
        encargado.setHoraEntrada(dto.getHoraEntrada());
        encargado.setHoraSalida(dto.getHoraSalida());

        return encargado;
    }
}
