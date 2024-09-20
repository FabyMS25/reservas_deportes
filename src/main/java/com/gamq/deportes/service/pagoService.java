package com.gamq.deportes.service;

import com.gamq.deportes.dto.pagosDto;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface pagoService {

    pagosDto findByUuid(String uuid);
    List<pagosDto> obtenerPagos();
    List<pagosDto> buscarPorFechaPago(Date fechaIni, Date fechaFin);
    pagosDto crearPago(pagosDto pagosDto);
    pagosDto actualizarPago(pagosDto pagosDto);
    String eliminarPago(String uuid);
}
