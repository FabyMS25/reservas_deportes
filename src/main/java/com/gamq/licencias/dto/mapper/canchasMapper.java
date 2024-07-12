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
        canchasDto.setCodigo(canchas.getCodigo());
        canchasDto.setTipo(canchas.getTipo());

        return canchasDto;
    }
    public static canchas toEntity(canchasDto canchasDto){
        canchas canchas =new canchas();
        canchas.setUuid(canchasDto.getUuid());

        return canchas;
    }

   /* private static Point setLatLgtCoordinates(Double pointX, Double pointY) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate coordinate = new Coordinate();
        coordinate.x = pointX;
        coordinate.y = pointY;

        Point latLgt = geometryFactory.createPoint(coordinate);
        return latLgt;
    }*/
}