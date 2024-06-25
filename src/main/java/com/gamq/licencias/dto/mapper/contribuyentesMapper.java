package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.contribuyenteDto;
import com.gamq.licencias.model.contribuyentes;

public class contribuyentesMapper {
    public static contribuyenteDto toDto(contribuyentes contri){
        contribuyenteDto contribuyenteDto = new contribuyenteDto();
        contribuyenteDto.setUuid(contri.getUuid());
        contribuyenteDto.setEstado(contri.isEstado());
        contribuyenteDto.setNombre(contri.getNombre());
        contribuyenteDto.setApPaterno(contri.getApPaterno());
        contribuyenteDto.setApMaterno(contri.getApMaterno());
        contribuyenteDto.setNroCarnet(contri.getNroCarnet());
        contribuyenteDto.setCiExpedido(contri.getCiExpedido());
        contribuyenteDto.setNroNit(contri.getNroNit());
        contribuyenteDto.setDireccion(contri.getDireccion());
        contribuyenteDto.setCelular(contribuyenteDto.getCelular());
        contribuyenteDto.setCorreo(contri.getCorreo());
        contribuyenteDto.setSexo(contri.getSexo());
        contribuyenteDto.setUserName(contri.getUserName());
        contribuyenteDto.setPassword(contri.getPassword());

        return contribuyenteDto;
    }
    public static contribuyentes toEntity(contribuyenteDto contri){
        contribuyentes contribuyentes=new contribuyentes();
        contribuyentes.setUuid(contri.getUuid());
        contribuyentes.setEstado(contri.isEstado());
        contribuyentes.setNombre(contri.getNombre());
        contribuyentes.setApPaterno(contri.getApPaterno());
        contribuyentes.setApMaterno(contri.getApMaterno());
        contribuyentes.setNroCarnet(contri.getNroCarnet());
        contribuyentes.setCiExpedido(contri.getCiExpedido());
        contribuyentes.setNroNit(contri.getNroNit());
        contribuyentes.setDireccion(contri.getDireccion());
        contribuyentes.setCelular(contri.getCelular());
        contribuyentes.setCorreo(contri.getCorreo());
        contribuyentes.setSexo(contri.getSexo());
        contribuyentes.setUserName(contri.getUserName());
        contribuyentes.setPassword(contri.getPassword());

        return contribuyentes;
    }
}
