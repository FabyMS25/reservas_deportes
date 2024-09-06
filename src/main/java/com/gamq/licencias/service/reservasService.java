package com.gamq.licencias.service;


import com.gamq.licencias.dto.pagosDto;
import com.gamq.licencias.dto.reservasDto;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface reservasService {

    reservasDto findByUuid(String uuid);
    List<reservasDto> findReservaByCancha(String uuid);
    List<reservasDto> obtenerReservas();
    List<reservasDto> buscarPorFechaPago(Date fechaIni, Date fechaFin);
    reservasDto crearReserva(reservasDto reservasDto);
    reservasDto actualizarReserva(reservasDto reservasDto);
    String eliminarReserva(String uuid);
}
