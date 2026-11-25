package com.gamq.deportes.service;

import com.gamq.deportes.dto.ClasificadorRuatDto;
import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.dto.ReservaBorderouxDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Dedicated application boundary for post-event Borderoux pre-liquidations.
 * A Borderoux record is persisted as a Reserva parent plus its declared
 * activities/talonarios, but it is not a booking or schedule operation.
 */
public interface ReservaBorderouxService {
    Page<ReservaBorderouxDto> obtenerConFiltros(Pageable pageable, Integer tab, String searchTerm,
                                                String estadoReserva, String organizacion);
    ReservaBorderouxDto findByUuid(String uuid);
    ReservaBorderouxDto crear(ReservaBorderouxDto reservaDto);
    ClasificadorRuatDto obtenerClasificador();
}
