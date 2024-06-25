package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.canchasDto;
import com.gamq.licencias.model.canchas;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class canchasMapper {

    public static canchasDto toDto(canchas canchas){
        canchasDto canchasDto= new canchasDto();
        canchasDto.setUuid(canchas.getUuid());
        canchasDto.setDistrito(canchas.getDistrito());
        canchasDto.setEstado(canchas.isEstado());
        canchasDto.setTechado(canchas.getTechado());
        canchasDto.setSuperficie(canchas.getSuperficie());
        canchasDto.setTotalxhora(canchas.getTotalxhora());
        canchasDto.setNombre(canchas.getNombre());
        canchasDto.setUbicacion(canchas.getUbicacion());
        if (canchas.getGeoUbicacion() != null) {
            canchasDto.setLatitud(canchas.getGeoUbicacion().getX());
            canchasDto.setLongitud(canchas.getGeoUbicacion().getY());
        }

        return canchasDto;
    }
    public static canchas toEntity(canchasDto canchasDto){
        canchas canchas =new canchas();
        canchas.setUuid(canchasDto.getUuid());
        canchas.setDistrito(canchasDto.getDistrito());
        canchas.setEstado(canchasDto.isEstado());
        canchas.setTechado(canchasDto.getTechado());
        canchas.setSuperficie(canchasDto.getSuperficie());
        canchas.setNombre(canchasDto.getNombre());
        canchas.setTotalxhora(canchasDto.getTotalxhora());
        canchas.setUbicacion(canchasDto.getUbicacion());
        if (!canchasDto.getLatitud().isNaN() && !canchasDto.getLongitud().isNaN()) {
            canchas.setGeoUbicacion(setLatLgtCoordinates(canchasDto.getLatitud(), canchasDto.getLongitud()));
        }

        return canchas;
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