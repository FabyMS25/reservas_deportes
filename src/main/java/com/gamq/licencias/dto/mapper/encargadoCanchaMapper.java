package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.encargadoCanchaDto;
import com.gamq.licencias.model.encargadoComplejo;

public class encargadoCanchaMapper {
    public static encargadoCanchaDto toDto(encargadoComplejo encargadoComplejo){
        encargadoCanchaDto encargadoCanchaDto = new encargadoCanchaDto();
        encargadoCanchaDto.setUuid(encargadoComplejo.getUuid());
        encargadoCanchaDto.setUserUuid(encargadoComplejo.getUserUuid());
        encargadoCanchaDto.setTelefono(encargadoComplejo.getTelefono());
        encargadoCanchaDto.setHoraEntrada(encargadoComplejo.getHoraEntrada());
        encargadoCanchaDto.setHoraSalida(encargadoComplejo.getHoraSalida());
        encargadoCanchaDto.setEstado(encargadoComplejo.isEstado());
        encargadoCanchaDto.setCanchasDto(canchasMapper.toDto(encargadoComplejo.getCancha()));

        return encargadoCanchaDto;
    }
    public static encargadoComplejo toEntity(encargadoCanchaDto encargadoCanchaDto){
        encargadoComplejo encargadoComplejo =new encargadoComplejo();
        encargadoComplejo.setUuid(encargadoCanchaDto.getUuid());
        encargadoComplejo.setUserUuid(encargadoCanchaDto.getUserUuid());
        encargadoComplejo.setTelefono(encargadoCanchaDto.getTelefono());
        encargadoComplejo.setHoraEntrada(encargadoCanchaDto.getHoraEntrada());
        encargadoComplejo.setHoraSalida(encargadoCanchaDto.getHoraSalida());
        encargadoComplejo.setEstado(encargadoComplejo.isEstado());

        return encargadoComplejo;
    }
}
