package com.gamq.deportes.service;

import com.gamq.deportes.dto.ReservaPeriodoDto;
import com.gamq.deportes.dto.ReservaPeriodoFilter;
import com.gamq.deportes.dto.ReservaPuntualDto;
import com.gamq.deportes.dto.ReservaPuntualFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ReservaPeriodoService {
    List<ReservaPeriodoDto> obtenerTodos();
    Page<ReservaPeriodoDto> obtenerReservasConFiltros(ReservaPeriodoFilter filter, Pageable pageable);
    List<ReservaPeriodoDto> findByEstudianteUuid(String estudianteUuid);
    ReservaPeriodoDto findByUuid(String uuid);
    List<ReservaPeriodoDto> findByReservaUuid(String reservaUuid);
    List<ReservaPeriodoDto> findByPeriodo(String periodo);
    List<ReservaPeriodoDto> findByContribuyenteUuid(String contribuyenteUuid);
    List<ReservaPeriodoDto> findByOrganizacionUuid(String organizacionUuid);
    ReservaPeriodoDto crearReservaPeriodo(ReservaPeriodoDto reservaPeriodoDto);
    ReservaPeriodoDto actualizarReservaPeriodo(ReservaPeriodoDto reservaPeriodoDto);
    String eliminarReservaPeriodo(String uuid);
}
