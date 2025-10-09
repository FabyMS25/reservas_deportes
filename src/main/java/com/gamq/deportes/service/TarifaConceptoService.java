package com.gamq.deportes.service;

import com.gamq.deportes.dto.TarifaConceptoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TarifaConceptoService {
    TarifaConceptoDto findByUuid(String uuid);
    List<TarifaConceptoDto> obtenerConceptos();
    List<TarifaConceptoDto> findConceptosByTarifa(String uuidTarifa);
    TarifaConceptoDto crearConcepto(TarifaConceptoDto conceptoDto);
    TarifaConceptoDto actualizarConcepto(TarifaConceptoDto conceptoDto);
    String eliminarConcepto(String uuid);
    String eliminarConceptoPermanente(String uuid);
}
