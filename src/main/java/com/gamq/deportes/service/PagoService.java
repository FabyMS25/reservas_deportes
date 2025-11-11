package com.gamq.deportes.service;

import com.gamq.deportes.dto.PagoDto;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface PagoService {

    PagoDto findByUuid(String uuid);
    List<PagoDto> obtenerPagos();
    List<PagoDto> buscarPorFechaPago(Date fechaIni, Date fechaFin);
    PagoDto crearPago(PagoDto pagosDto);
    PagoDto actualizarPago(PagoDto pagosDto);
    String eliminarPago(String uuid);
}
