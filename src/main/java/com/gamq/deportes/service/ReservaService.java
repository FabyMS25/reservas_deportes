package com.gamq.deportes.service;


import com.gamq.deportes.dto.ReservaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public interface ReservaService {

    ReservaDto findByUuid(String uuid);
    List<ReservaDto> findReservasByCampoDeportivo(String uuid);
    List<ReservaDto> findReservasByContribuyente(String uuid);

    ReservaDto findByNumeroReserva(String numeroReserva);
    List<ReservaDto> findByEstadoReserva(String estadoReserva);
    List<ReservaDto> findByTipoReserva(String tipoReserva);
    List<ReservaDto> obtenerReservas();
    Page<ReservaDto> obtenerReservas(Pageable pageable);
    List<ReservaDto> buscarPorFechaReserva(LocalDateTime fechaIni, LocalDateTime fechaFin);
    Page<ReservaDto> buscarPorFechaReserva(LocalDateTime fechaIni, LocalDateTime fechaFin, Pageable pageable);
    List<ReservaDto> findReservasActivasPorCampoYFecha(String uuidCampo, LocalDateTime fechaIni, LocalDateTime fechaFin);
    boolean existeReservaConflictiva(String uuidCampo, LocalDateTime fechaIni, LocalDateTime fechaFin, String excludeReservaUuid) ;
    List<ReservaDto> findReservasConflictivas(String uuidCampo, LocalDateTime fechaIni, LocalDateTime fechaFin, String excludeReservaUuid);
    ReservaDto crearReserva(ReservaDto reservasDto);
    ReservaDto actualizarReserva(ReservaDto reservasDto);
    ReservaDto aprobarReserva(String uuid, String aprobadoPor);
    ReservaDto rechazarReserva(String uuid, String motivoRechazo, String aprobadoPor);
    ReservaDto cancelarReserva(String uuid, String motivo);
    String eliminarReserva(String uuid);
}
