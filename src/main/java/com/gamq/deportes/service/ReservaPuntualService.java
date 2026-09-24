package com.gamq.deportes.service;

import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.dto.ReservaPuntualDto;
import com.gamq.deportes.dto.ReservaPuntualFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public interface ReservaPuntualService {
    List<ReservaPuntualDto> obtenerReservas();
    Page<ReservaPuntualDto> obtenerReservas(Pageable pageable);
    Page<ReservaPuntualDto> obtenerReservasConFiltros(ReservaPuntualFilter filter, Pageable pageable);
    Page<ReservaPuntualDto> checkAndUpdateExpiredReservations();
    ReservaPuntualDto findByUuid(String uuid);
    ReservaPuntualDto findByReservaUuid(String reservaUuid);
    List<ReservaPuntualDto> findByFechaUso(LocalDate fechaUso);
    List<ReservaPuntualDto> findByFechaUsoBetween(LocalDate fechaInicio, LocalDate fechaFin);
    List<ReservaPuntualDto> findByCampoDeportivoAndFechaUso(String campoUuid, LocalDate fechaUso);
    List<ReservaPuntualDto> findReservasActivasPorCampoYFecha(String campoUuid, LocalDate fechaUso);
    Page<ReservaPuntualDto> findByCampoDeportivoUuid(String campoUuid, Pageable pageable);
    Long countByCampoDeportivoAndFechaUso(String campoUuid, LocalDate fechaUso);
    ReservaPuntualDto registrarUsoReal(String uuid, LocalDateTime fechaUsoReal);
    List<ReservaPuntualDto> findByCategoria(String categoria);
    List<ReservaPuntualDto> findByCantidadPersonasBetween(Integer cantidadMin, Integer cantidadMax);
    List<ReservaPuntualDto> findReservasConflictivas(String campoUuid, LocalDate fechaUso, LocalTime horaInicio, LocalTime horaFin, String excludeReservaUuid);
    boolean existeReservaConflictiva(String campoUuid, LocalDate fechaUso, LocalTime horaInicio, LocalTime horaFin, String excludeReservaUuid);
    ReservaPuntualDto crearReservaPuntual(ReservaPuntualDto ReservaPuntualDto);
    ReservaPuntualDto actualizarReservaPuntual(ReservaPuntualDto ReservaPuntualDto);
    String eliminarReserva(String uuid);
}
