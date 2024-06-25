package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.encargadoCanchaDto;
import com.gamq.licencias.model.encargadoCancha;

public class encargadoCanchaMapper {
    public static encargadoCanchaDto toDto(encargadoCancha encargadoCancha){
        encargadoCanchaDto encargadoCanchaDto = new encargadoCanchaDto();
        encargadoCanchaDto.setUuid(encargadoCancha.getUuid());
        encargadoCanchaDto.setUserId(encargadoCancha.getUserId());
        encargadoCanchaDto.setTelefono(encargadoCancha.getTelefono());
        encargadoCanchaDto.setHoraEntrada(encargadoCancha.getHoraEntrada());
        encargadoCanchaDto.setHoraSalida(encargadoCancha.getHoraSalida());
        encargadoCanchaDto.setEstado(encargadoCancha.isEstado());
        encargadoCanchaDto.setCanchasDto(canchasMapper.toDto(encargadoCancha.getCancha()));

        return encargadoCanchaDto;
    }
    public static encargadoCancha toEntity(encargadoCanchaDto encargadoCanchaDto){
        encargadoCancha encargadoCancha =new encargadoCancha();
        encargadoCancha.setUuid(encargadoCanchaDto.getUuid());
        encargadoCancha.setUserId(encargadoCanchaDto.getUserId());
        encargadoCancha.setTelefono(encargadoCanchaDto.getTelefono());
        encargadoCancha.setHoraEntrada(encargadoCanchaDto.getHoraEntrada());
        encargadoCancha.setHoraSalida(encargadoCanchaDto.getHoraSalida());
        encargadoCancha.setEstado(encargadoCancha.isEstado());

        return encargadoCancha;
    }
}
