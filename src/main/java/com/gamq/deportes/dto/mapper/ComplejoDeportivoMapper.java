package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ComplejoDeportivoDto;
import com.gamq.deportes.model.ComplejoDeportivo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.stream.Collectors;

public class ComplejoDeportivoMapper {
    public static ComplejoDeportivoDto toDto(ComplejoDeportivo complejo) {
        ComplejoDeportivoDto complejoDto = new ComplejoDeportivoDto();

        complejoDto.setUuid(complejo.getUuid());
        complejoDto.setEstado(complejo.isEstado());
        complejoDto.setFechaCreacion(complejo.getFechaCreacion());

        complejoDto.setNombre(complejo.getNombre());
        complejoDto.setTelefonoPrincipal(complejo.getTelefonoPrincipal());
        complejoDto.setTipoEstructura(complejo.getTipoEstructura());
        complejoDto.setEstadoActual(complejo.getEstadoActual());
        complejoDto.setTieneVestuarios(complejo.getTieneVestuarios());
        complejoDto.setTieneGraderias(complejo.getTieneGraderias());
        complejoDto.setTipoEstructura(complejo.getTipoEstructura());
        complejoDto.setObservaciones(complejo.getObservaciones());
//        compleDto.setTechado(complejo.getTechado());
        complejoDto.setDistrito(complejo.getDistrito());
        complejoDto.setUbicacion(complejo.getUbicacion());
//        if (complejo.getGeoUbicacion() != null && !complejo.getGeoUbicacion().isEmpty()) {
//            Point point = (Point) complejo.getGeoUbicacion();
//            complejoDto.setLatitud(point.getX());
//            complejoDto.setLongitud(point.getY());
//        };
        if (complejo.getGeoUbicacion() != null) {
            complejoDto.setLatitud(complejo.getGeoUbicacion().getX());
            complejoDto.setLongitud(complejo.getGeoUbicacion().getY());
        }
        complejoDto.setEncargadosDtoList(
                complejo.getEncargadosList().stream().map(encargado -> {
                    return EncargadoComplejoMapper.toDto(encargado, false);
                }).collect(Collectors.toList())
        );

        complejoDto.setCamposDeportivosDtoList(
                complejo.getCamposDeportivosList().stream().map(cancha -> {
                    return CampoDeportivoMapper.toDto(cancha, false);
                }).collect(Collectors.toList())
        );

        return complejoDto;
    }

    public static ComplejoDeportivo toEntity(ComplejoDeportivoDto complejoDto) {
        ComplejoDeportivo complejo = new ComplejoDeportivo();

        complejo.setUuid(complejoDto.getUuid());
        complejo.setEstado(complejoDto.isEstado());

        complejo.setNombre(complejoDto.getNombre());
        complejo.setTelefonoPrincipal(complejoDto.getTelefonoPrincipal());
        complejo.setDistrito(complejoDto.getDistrito());
        complejo.setUbicacion(complejoDto.getUbicacion());
        complejo.setTipoEstructura(complejoDto.getTipoEstructura());
        complejo.setTieneVestuarios(complejoDto.getTieneVestuarios());
        complejo.setTieneGraderias(complejoDto.getTieneGraderias());
        complejo.setTipoEstructura(complejoDto.getTipoEstructura());
        complejo.setEstadoActual(complejoDto.getEstadoActual());
        complejo.setObservaciones(complejoDto.getObservaciones());

        // Convert lat/long to geometry
//        if (complejoDto.hasGeoLocation()) {
//            complejo.setGeoUbicacion(createPoint(complejoDto.getLatitud(), complejoDto.getLongitud()));
//        }
        if (!complejoDto.getLatitud().isNaN() && !complejoDto.getLatitud().isNaN()) {
            complejo.setGeoUbicacion(setLatLgtCoordinates(complejoDto.getLatitud(), complejoDto.getLongitud()));
        }
        return complejo;
    }


    public static ComplejoDeportivo updateEntityFromDto(ComplejoDeportivoDto complejoDto, ComplejoDeportivo complejo) {
        complejo.setNombre(complejoDto.getNombre());
        complejo.setTelefonoPrincipal(complejoDto.getTelefonoPrincipal());
        complejo.setUbicacion(complejoDto.getUbicacion());
        complejo.setTipoEstructura(complejoDto.getTipoEstructura());
        complejo.setEstadoActual(complejoDto.getEstadoActual());
        complejo.setObservaciones(complejoDto.getObservaciones());

        if (!complejoDto.getLatitud().isNaN() && !complejoDto.getLatitud().isNaN()) {
            complejo.setGeoUbicacion(setLatLgtCoordinates(complejoDto.getLatitud(), complejoDto.getLongitud()));
        }

        return complejo;
    }
    public static ComplejoDeportivoDto toSimpleDto(ComplejoDeportivo complejo) {
        ComplejoDeportivoDto dto = new ComplejoDeportivoDto();

        dto.setUuid(complejo.getUuid());
        dto.setNombre(complejo.getNombre());
        dto.setTelefonoPrincipal(complejo.getTelefonoPrincipal());
        dto.setUbicacion(complejo.getUbicacion());
        dto.setTipoEstructura(complejo.getTipoEstructura());
        dto.setEstadoActual(complejo.getEstadoActual());

        if (complejo.getGeoUbicacion() != null && !complejo.getGeoUbicacion().isEmpty()) {
            Point point = (Point) complejo.getGeoUbicacion();
            dto.setLatitud(point.getX());
            dto.setLongitud(point.getY());
        }

        return dto;
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
