package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.TableroDto;
import com.gamq.deportes.model.Tablero;

public class TableroMapper {

    public static TableroDto toDto(Tablero tabla){
        TableroDto tableroDto = new TableroDto();
            tableroDto.setUuid(tabla.getUuid());
            tableroDto.setHorario(tabla.getHorario());
            tableroDto.setEstado(tabla.isEstado());

        return tableroDto;
    }

    public static Tablero toEntity(TableroDto tableroDto){
        Tablero tabla=new Tablero();
            tabla.setUuid(tableroDto.getUuid());
            tabla.setHorario(tableroDto.getHorario());
            tabla.setEstado(tableroDto.isEstado());
        return tabla;
    }
}
