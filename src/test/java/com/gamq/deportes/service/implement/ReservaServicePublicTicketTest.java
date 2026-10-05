package com.gamq.deportes.service.implement;

import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServicePublicTicketTest {
    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaServiceImpl service;

    @Test
    void renewsPublicIdAndPersistsIt() {
        UUID oldPublicId = UUID.randomUUID();
        Reserva reserva = new Reserva()
                .setUuid("reservation-uuid")
                .setPublicId(oldPublicId)
                .setEstadoReserva("PAGADA");
        when(reservaRepository.findByUuid("reservation-uuid")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(reserva)).thenReturn(reserva);

        String newPublicId = service.renovarPublicIdTicket("reservation-uuid");

        assertNotEquals(oldPublicId, UUID.fromString(newPublicId));
        assertEquals(UUID.fromString(newPublicId), reserva.getPublicId());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void doesNotRenewPublicIdForCanceledReservation() {
        Reserva reserva = new Reserva()
                .setUuid("reservation-uuid")
                .setPublicId(UUID.randomUUID())
                .setEstadoReserva("CANCELADA");
        when(reservaRepository.findByUuid("reservation-uuid")).thenReturn(Optional.of(reserva));

        assertThrows(IllegalStateException.class,
                () -> service.renovarPublicIdTicket("reservation-uuid"));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }
}
