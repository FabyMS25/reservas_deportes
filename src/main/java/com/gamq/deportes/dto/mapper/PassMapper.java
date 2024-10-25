package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.PassDto;
import com.gamq.deportes.model.Pass;

public class PassMapper {

    public static PassDto toDto(Pass pass){
        PassDto passDto = new PassDto();
        passDto.setUuid(pass.getUuid());
        passDto.setEmail(pass.getEmail());
        passDto.setToken(pass.getToken());
        passDto.setExpiresAt(pass.getExpiresAt());
        passDto.setEstado(pass.isEstado());
        return passDto;
    }

    public static Pass toEntity(PassDto passDto){
        Pass pass = new Pass();
        pass.setUuid(passDto.getUuid());
        pass.setEmail(passDto.getEmail());
        pass.setToken(passDto.getToken());
        pass.setExpiresAt(passDto.getExpiresAt());
        pass.setEstado(passDto.isEstado());

        return pass;
    }
}
