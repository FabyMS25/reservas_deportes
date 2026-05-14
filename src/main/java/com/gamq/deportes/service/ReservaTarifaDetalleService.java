package com.gamq.deportes.service;

import com.gamq.deportes.dto.ReservaTarifaDetalleDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ReservaTarifaDetalleService {
    ReservaTarifaDetalleDto findByUuid(String uuid);
    List<ReservaTarifaDetalleDto> obtenerReservaTarifaDetalles();
    List<ReservaTarifaDetalleDto> findByReservaUuid(String reservaUuid);
    ReservaTarifaDetalleDto crearReservaTarifaDetalle(ReservaTarifaDetalleDto reservaTarifaDetalleDto);
    ReservaTarifaDetalleDto actualizarReservaTarifaDetalle(ReservaTarifaDetalleDto reservaTarifaDetalleDto);
    String delete(String uuid);
}
