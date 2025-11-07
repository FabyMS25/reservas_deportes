package com.gamq.deportes.service;

import com.gamq.deportes.dto.ReservaPeriodoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ReservaPeriodoService {

    ReservaPeriodoDto findByUuid(String uuid);
    ReservaPeriodoDto findByReservaUuid(String reservaUuid);
    List<ReservaPeriodoDto> findByPeriodo(String periodo);
    List<ReservaPeriodoDto> findByContribuyenteUuid(String contribuyenteUuid);
    List<ReservaPeriodoDto> findByOrganizacionUuid(String organizacionUuid);
    List<ReservaPeriodoDto> obtenerTodos();
    ReservaPeriodoDto crearReservaPeriodo(ReservaPeriodoDto reservaPeriodoDto);
    ReservaPeriodoDto actualizarReservaPeriodo(ReservaPeriodoDto reservaPeriodoDto);
    String eliminarReservaPeriodo(String uuid);
}