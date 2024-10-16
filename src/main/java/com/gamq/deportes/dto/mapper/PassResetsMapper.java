package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.PassResetsDto;
import com.gamq.deportes.model.PassResets;

public class PassResetsMapper {

    public static PassResetsDto toDto(PassResets passResets){
        PassResetsDto passResetsDto = new PassResetsDto();
        passResetsDto.setUuid(passResets.getUuid());
        passResetsDto.setEmail(passResets.getEmail());
        passResetsDto.setToken(passResets.getToken());
        passResetsDto.setExprires_at(passResets.getExprires_at());
        passResetsDto.setEstado(passResets.isEstado());


        return passResetsDto;
    }

    public static PassResets toEntity(PassResetsDto passResetsDto){
        PassResets passResets= new PassResets();
        passResets.setUuid(passResetsDto.getUuid());
        passResets.setEmail(passResetsDto.getEmail());
        passResets.setToken(passResetsDto.getToken());
        passResets.setExprires_at(passResetsDto.getExprires_at());
        passResets.setEstado(passResetsDto.isEstado());

        return passResets;
    }
}
