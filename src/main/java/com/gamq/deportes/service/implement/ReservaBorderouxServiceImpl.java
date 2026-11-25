package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ClasificadorRuatDto;
import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.dto.ReservaBorderouxDto;
import com.gamq.deportes.service.ReservaBorderouxService;
import com.gamq.deportes.service.ReservaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ReservaBorderouxServiceImpl implements ReservaBorderouxService {

    private final ReservaService reservaService;

    public ReservaBorderouxServiceImpl(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaBorderouxDto> obtenerConFiltros(Pageable pageable, Integer tab, String searchTerm,
                                                        String estadoReserva, String organizacion) {
        return reservaService.obtenerReservas(pageable, tab, searchTerm, "borderoux",
                estadoReserva, null, null, organizacion, null, null, false).map(this::toBorderoux);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaBorderouxDto findByUuid(String uuid) {
        ReservaDto reserva = reservaService.findByUuid(uuid);
        if (!"borderoux".equalsIgnoreCase(reserva.getTipoReserva())) {
            throw new IllegalArgumentException("El registro solicitado no es una liquidación Borderoux");
        }
        return toBorderoux(reserva);
    }

    @Override
    public ReservaBorderouxDto crear(ReservaBorderouxDto reservaDto) {
        if (reservaDto == null) {
            throw new IllegalArgumentException("La liquidación Borderoux es requerida");
        }
        // These values belong to the Borderoux registration lifecycle, not to a
        // sports-field booking. The general persistence service owns validation
        // and server-side recalculation of its activities and talonarios.
        ReservaDto reserva = toReserva(reservaDto);
        return toBorderoux(reservaService.crearReserva(reserva));
    }

    @Override
    @Transactional(readOnly = true)
    public ClasificadorRuatDto obtenerClasificador() {
        return reservaService.obtenerClasificadorBorderoux();
    }

    private ReservaDto toReserva(ReservaBorderouxDto dto) {
        ReservaDto reserva = new ReservaDto();
        reserva.setUuid(dto.getUuid());
        reserva.setFechaCreacion(dto.getFechaCreacion() == null ? LocalDateTime.now() : dto.getFechaCreacion());
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setNumeroTasa(dto.getNumeroTasa());
        reserva.setMontoTotal(dto.getMontoTotal());
        reserva.setEstadoValidacion(dto.getEstadoValidacion() == null ? "APROBADA" : dto.getEstadoValidacion());
        reserva.setEstadoReserva(dto.getEstadoReserva() == null ? "PENDIENTE" : dto.getEstadoReserva());
        reserva.setClasificadorRuatId(dto.getClasificadorRuatId());
        reserva.setCreadoPor(dto.getCreadoPor());
        reserva.setOrganizacionDto(dto.getOrganizacionDto());
        reserva.setContribuyenteDto(dto.getContribuyenteDto());
        reserva.setBorderouxActividadesDtoList(dto.getBorderouxActividadesDtoList());
        reserva.setTipoReserva("borderoux");
        reserva.setCancelacionAutomatica(false);
        reserva.setEsReservaGratuita(false);
        return reserva;
    }

    private ReservaBorderouxDto toBorderoux(ReservaDto reserva) {
        return new ReservaBorderouxDto()
                .setUuid(reserva.getUuid())
                .setFechaCreacion(reserva.getFechaCreacion())
                .setFechaActualizacion(reserva.getFechaActualizacion())
                .setNumeroReserva(reserva.getNumeroReserva())
                .setNumeroTasa(reserva.getNumeroTasa())
                .setMontoTotal(reserva.getMontoTotal())
                .setEstadoReserva(reserva.getEstadoReserva())
                .setEstadoValidacion(reserva.getEstadoValidacion())
                .setClasificadorRuatId(reserva.getClasificadorRuatId())
                .setCreadoPor(reserva.getCreadoPor())
                .setOrganizacionDto(reserva.getOrganizacionDto())
                .setContribuyenteDto(reserva.getContribuyenteDto())
                .setBorderouxActividadesDtoList(reserva.getBorderouxActividadesDtoList());
    }
}
