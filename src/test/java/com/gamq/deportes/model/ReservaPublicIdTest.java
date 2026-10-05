package com.gamq.deportes.model;

import org.junit.jupiter.api.Test;
import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.dto.mapper.ReservaMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReservaPublicIdTest {
    @Test
    void createsRandomPublicIdAlongsideReservationUuid() {
        Reserva first = new Reserva();
        Reserva second = new Reserva();

        first.initializeUuidAndDates();
        second.initializeUuidAndDates();

        assertNotNull(first.getPublicId());
        assertEquals(4, first.getPublicId().version());
        assertNotEquals(first.getUuid(), first.getPublicId().toString());
        assertNotEquals(first.getPublicId(), second.getPublicId());
    }

    @Test
    void preservesPublicIdWhenReservationIsUpdated() {
        UUID publicId = UUID.randomUUID();
        Reserva reserva = new Reserva().setPublicId(publicId);

        reserva.initializeUuidAndDates();

        assertEquals(publicId, reserva.getPublicId());
        assertNotNull(reserva.getUuid());
    }

    @Test
    void mapsPublicIdToResponsesButNeverAcceptsItFromRequestDtos() {
        UUID publicId = UUID.randomUUID();

        ReservaDto response = ReservaMapper.toDto(new Reserva().setPublicId(publicId));
        Reserva requestEntity = ReservaMapper.toEntity(new ReservaDto().setPublicId(publicId));

        assertEquals(publicId, response.getPublicId());
        assertNull(requestEntity.getPublicId());
    }
}
