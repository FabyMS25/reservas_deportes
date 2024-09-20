package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.encargadoComplejoDto;
import com.gamq.deportes.model.encargadoComplejo;

public class encargadoComplejoMapper {
    public static encargadoComplejoDto toDto(encargadoComplejo encargadoComplejo, boolean op){
        encargadoComplejoDto encargadoComplejoDto = new encargadoComplejoDto();
        encargadoComplejoDto.setUuid(encargadoComplejo.getUuid());
        encargadoComplejoDto.setUserUuid(encargadoComplejo.getUserUuid());
        encargadoComplejoDto.setTelefono(encargadoComplejo.getTelefono());
        encargadoComplejoDto.setHoraEntrada(encargadoComplejo.getHoraEntrada());
        encargadoComplejoDto.setHoraSalida(encargadoComplejo.getHoraSalida());
        encargadoComplejoDto.setNombre(encargadoComplejo.getNombre());
        encargadoComplejoDto.setEstado(encargadoComplejo.isEstado());
        //encargadoComplejoDto.setComplejoDto(complejoMapper.toDto(encargadoComplejo.getComplejo()));
        if(op) {
            encargadoComplejoDto.setComplejoDto(complejoMapper.toDto(encargadoComplejo.getComplejo()));
        }
        return encargadoComplejoDto;
    }
    public static encargadoComplejo toEntity(encargadoComplejoDto encargadoComplejoDto){
        encargadoComplejo encargadoComplejo =new encargadoComplejo();
        encargadoComplejo.setUuid(encargadoComplejoDto.getUuid());
        encargadoComplejo.setUserUuid(encargadoComplejoDto.getUserUuid());
        encargadoComplejo.setTelefono(encargadoComplejoDto.getTelefono());
        encargadoComplejo.setHoraEntrada(encargadoComplejoDto.getHoraEntrada());
        encargadoComplejo.setHoraSalida(encargadoComplejoDto.getHoraSalida());
        encargadoComplejo.setNombre(encargadoComplejoDto.getNombre());
        encargadoComplejo.setEstado(encargadoComplejo.isEstado());

        return encargadoComplejo;
    }
}
