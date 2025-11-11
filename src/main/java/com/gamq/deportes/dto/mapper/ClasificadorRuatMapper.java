package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ClasificadorRuatDto;
import com.gamq.deportes.model.ClasificadorRuat;

public class ClasificadorRuatMapper {

    public static ClasificadorRuatDto toDto(ClasificadorRuat clasificador) {
        if (clasificador == null) return null;

        ClasificadorRuatDto dto = new ClasificadorRuatDto();
        dto.setUuid(clasificador.getUuid());
        dto.setCodigo(clasificador.getCodigo());
        dto.setNombre(clasificador.getNombre());
        dto.setDescripcion(clasificador.getDescripcion());
//        dto.setTipoReserva(clasificador.getTipoReserva());
        dto.setTipoArancel(clasificador.getTipoArancel());
        dto.setActivo(clasificador.getActivo());
        dto.setFechaCreacion(clasificador.getFechaCreacion());
        dto.setEstado(clasificador.isEstado());

        return dto;
    }

    public static ClasificadorRuat toEntity(ClasificadorRuatDto dto) {
        if (dto == null) return null;

        ClasificadorRuat clasificador = new ClasificadorRuat();
        clasificador.setUuid(dto.getUuid());
        clasificador.setCodigo(dto.getCodigo());
        clasificador.setNombre(dto.getNombre());
        clasificador.setDescripcion(dto.getDescripcion());
//        clasificador.setTipoReserva(dto.getTipoReserva());
        clasificador.setTipoArancel(dto.getTipoArancel());
        clasificador.setActivo(dto.getActivo());
        clasificador.setFechaCreacion(dto.getFechaCreacion());
        clasificador.setEstado(dto.isEstado());

        return clasificador;
    }

    public static ClasificadorRuat updateEntityFromDto(ClasificadorRuatDto dto, ClasificadorRuat clasificador) {
        if (dto == null || clasificador == null) return clasificador;

        clasificador.setCodigo(dto.getCodigo());
        clasificador.setNombre(dto.getNombre());
        clasificador.setDescripcion(dto.getDescripcion());
//        clasificador.setTipoReserva(dto.getTipoReserva());
        clasificador.setTipoArancel(dto.getTipoArancel());
        clasificador.setActivo(dto.getActivo());

        return clasificador;
    }
}