package com.gamq.deportes.service;

import com.gamq.deportes.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Service
public interface ReservaRecurrenteService {
    List<ReservaRecurrenteDto> obtenerReservas();
    Page<ReservaRecurrenteDto> obtenerReservas(Pageable pageable);
    Page<ReservaRecurrenteDto> obtenerReservasConFiltros(ReservaRecurrenteFilter filter, Pageable pageable);
    Page<ReservaRecurrenteDto> checkAndUpdateExpiredReservations();
    ReservaRecurrenteDto findByUuid(String uuid);
    ReservaRecurrenteDto findByReservaUuid(String reservaUuid);
    List<ReservaRecurrenteDto> findByTipoRecurrencia(String tipoRecurrencia);
    List<ReservaRecurrenteDto> findByActivo(Boolean activo);
    List<ReservaRecurrenteDto> findActivasEnFecha(LocalDate fecha);

    List<ReservaRecurrenteDto> findActivasEnFechaPorCampo(String campoUuid, LocalDate fecha);
    List<ReservaRecurrenteDto> findActivasVigentes();
    List<ReservaRecurrenteDto> findActivasPorDiaSemana(String diaSemana, LocalDate fecha);
    List<ReservaRecurrenteDto> findPorVencer(LocalDate fechaLimite);
    List<ReservaRecurrenteDto> findByCategoria(String categoria);
    Page<ReservaRecurrenteDto> findByCampoDeportivoUuid(String campoUuid, Pageable pageable);
    ReservaRecurrenteDto activarReservaRecurrente(String uuid);
    ReservaRecurrenteDto desactivarReservaRecurrente(String uuid);
    ReservaRecurrenteDto crearReservaRecurrente(ReservaRecurrenteDto ReservaRecurrenteDto);
    ReservaRecurrenteDto actualizarReservaRecurrente(ReservaRecurrenteDto ReservaPuntualDto);
    String eliminarReserva(String uuid);
}
