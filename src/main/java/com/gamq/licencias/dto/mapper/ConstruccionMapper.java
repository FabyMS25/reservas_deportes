package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.ConstruccionDto;
import com.gamq.licencias.model.Construccion;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class ConstruccionMapper {

    public static ConstruccionDto ToDto(Construccion construccion){
        ConstruccionDto construccionDto = new ConstruccionDto();
        construccionDto.setUuid(construccion.getUuid());
        construccionDto.setCodInmueble (construccion.getCodInmueble());
        construccionDto.setCiTitular(construccion.getCiTitular());
        construccionDto.setNombreTitular(construccion.getNombreTitular());
        construccionDto.setAppTitular(construccion.getAppTitular());
        construccionDto.setApmTitular(construccion.getApmTitular());
        construccionDto.setDireccion(construccion.getDireccion());
        construccionDto.setSuperficie(construccion.getSuperficie());
        construccionDto.setObservacion(construccion.getObservacion());
        if (construccion.getGeoUbicacion() != null) {
            construccionDto.setLatitud(construccion.getGeoUbicacion().getX());
            construccionDto.setLongitud(construccion.getGeoUbicacion().getY());
        }
        construccionDto.setObservacion(construccion.getObservacion());


        return construccionDto;
    }
    public static Construccion ToEntity(ConstruccionDto construccionDto){
        Construccion construccion = new Construccion();
        construccion.setUuid(construccionDto.getUuid());
        construccion.setCodInmueble(construccionDto.getCodInmueble());
        construccion.setCiTitular(construccionDto.getCiTitular());
        construccion.setNombreTitular(construccionDto.getNombreTitular());
        construccion.setAppTitular(construccionDto.getAppTitular());
        construccion.setApmTitular(construccionDto.getApmTitular());
        construccion.setDireccion(construccionDto.getDireccion());
        construccion.setSuperficie(construccionDto.getSuperficie());
        construccion.setObservacion(construccionDto.getObservacion());
        if (!construccionDto.getLatitud().isNaN() && !construccionDto.getLatitud().isNaN()) {
            construccion.setGeoUbicacion(setLatLgtCoordinates(construccionDto.getLatitud(), construccionDto.getLongitud()));
        }

        return construccion;
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
