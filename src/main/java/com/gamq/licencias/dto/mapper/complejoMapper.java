package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.complejoDto;
import com.gamq.licencias.model.complejo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.stream.Collectors;

public class complejoMapper {
    public static complejoDto toDto(complejo complejo) {
        complejoDto compleDto = new complejoDto();
        compleDto.setUuid(complejo.getUuid());
        compleDto.setNombre(complejo.getNombre());
        compleDto.setDistrito(complejo.getDistrito());
        compleDto.setTechado(complejo.getTechado());
        compleDto.setUbicacion(complejo.getUbicacion());
        if (complejo.getGeoUbicacion() != null) {
            compleDto.setLatitud(complejo.getGeoUbicacion().getX());
            compleDto.setLongitud(complejo.getGeoUbicacion().getY());
        }

        compleDto.setEncargadosDtoList(
                complejo.getEncargadoComplejoList().stream().map(encargado -> {
                    return encargadoComplejoMapper.toDto(encargado, false);
                }).collect(Collectors.toList())
        );

        compleDto.setEstado(complejo.isEstado());
        return compleDto;
    }

    public static complejo toEntity(complejoDto complejoDto) {
        complejo comple = new complejo();
        comple.setUuid(complejoDto.getUuid());
        comple.setDistrito(complejoDto.getDistrito());
        comple.setNombre(complejoDto.getNombre());
        comple.setTechado(complejoDto.getTechado());
        comple.setUbicacion(complejoDto.getUbicacion());
        if (!complejoDto.getLatitud().isNaN() && !complejoDto.getLatitud().isNaN()) {
            comple.setGeoUbicacion(setLatLgtCoordinates(complejoDto.getLatitud(), complejoDto.getLongitud()));
        }
        return comple;
    }

    private static Point setLatLgtCoordinates(Double pointX, Double pointY) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate coordinate = new Coordinate();
        coordinate.x = pointX;
        coordinate.y = pointY;

        Point latLgt = geometryFactory.createPoint(coordinate);
        return latLgt;
    }
}
