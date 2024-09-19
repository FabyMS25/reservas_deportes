package com.gamq.licencias.service;

import com.gamq.licencias.dto.TarifaDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TarifasService {
    TarifaDto findByUuid(String uuid);
    List<TarifaDto> obtenerTarifas();
    TarifaDto crearTarifa(TarifaDto TarifaDto);
    TarifaDto actualizarTarifa(TarifaDto TarifaDto);
    String deleteTarifa(String uuid);
}
