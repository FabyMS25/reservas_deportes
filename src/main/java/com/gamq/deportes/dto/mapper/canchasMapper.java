package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.canchasDto;
import com.gamq.deportes.model.canchas;

import java.util.stream.Collectors;

public class canchasMapper {

    public static canchasDto toDto(canchas canchas){
        canchasDto canchasDto= new canchasDto();
        canchasDto.setUuid(canchas.getUuid());
        canchasDto.setCodigo(canchas.getCodigo());
        canchasDto.setTipo(canchas.getTipo());
        canchasDto.setEstado(canchas.isEstado());
        canchasDto.setComplejoDto(complejoMapper.toDto(canchas.getComplejo()));
        canchasDto.setSuperficieDto(SuperficieMapper.toDto(canchas.getSuperficie()));

        canchasDto.setTarifasDtoList(
        canchas.getTarifasList().stream().map(tarifa -> {
                   return TarifaMapper.toDto(tarifa, false);
                }).collect(Collectors.toList())
        );
        canchasDto.setHorarioDtoList(
            canchas.getHorarioList().stream().map(horario->{
                return horarioMapper.toDTo(horario, false);
                }).collect(Collectors.toList())
        );


        return canchasDto;
    }

    public static canchas toEntity(canchasDto canchasDto){
        canchas canchas =new canchas();
        canchas.setUuid(canchasDto.getUuid());
        canchas.setCodigo(canchasDto.getCodigo());
        canchas.setTipo(canchasDto.getTipo());
        canchas.setEstado(canchasDto.isEstado());
        return canchas;
    }


}