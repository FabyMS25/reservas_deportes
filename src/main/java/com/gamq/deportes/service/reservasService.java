package com.gamq.deportes.service;


import com.gamq.deportes.dto.ReservaDto;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface reservasService {

    ReservaDto findByUuid(String uuid);
    List<ReservaDto> findReservaByCancha(String uuid);
    List<ReservaDto> obtenerReservas();
    List<ReservaDto> buscarPorFechaPago(Date fechaIni, Date fechaFin);
    ReservaDto crearReserva(ReservaDto reservasDto);
    ReservaDto actualizarReserva(ReservaDto reservasDto);
    String eliminarReserva(String uuid);
}
