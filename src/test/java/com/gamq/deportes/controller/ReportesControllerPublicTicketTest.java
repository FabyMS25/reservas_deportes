package com.gamq.deportes.controller;

import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.Tarifa;
import com.gamq.deportes.repository.ReservaRepository;
import com.gamq.deportes.repository.ReservaTarifaDetalleRepository;
import com.gamq.deportes.service.ReservaRecurrenteService;
import com.gamq.deportes.service.SistemaAuditoriaService;
import com.gamq.deportes.utils.GeneradorReporte;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportesControllerPublicTicketTest {
    @Mock
    private GeneradorReporte generadorReporte;
    @Mock
    private ReservaRecurrenteService reservaRecurrenteService;
    @Mock
    private SistemaAuditoriaService sistemaAuditoriaService;
    @Mock
    private ReservaTarifaDetalleRepository reservaTarifaDetalleRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private ReportesController controller;

    @Test
    void streamsOneTimeTicketWithoutPubliclyCachingPdf() throws Exception {
        UUID publicId = UUID.randomUUID();
        Reserva reserva = new Reserva()
                .setPublicId(publicId)
                .setUuid("reservation-uuid")
                .setTipoReserva("puntual")
                .setEstadoReserva("PAGADA")
                .setTarifa(new Tarifa().setTipoCobro("por_evento"));
        when(reservaRepository.findByPublicId(publicId)).thenReturn(Optional.of(reserva));
        when(reservaTarifaDetalleRepository.findByReservaUuid("reservation-uuid"))
                .thenReturn(Collections.emptyList());

        controller.publicTicket(publicId.toString(), response);

        verify(generadorReporte).generarSqlReportePdfNoCache(
                eq("ticket"),
                eq("classpath:reportes/ticketPuntualEven.jrxml"),
                any(),
                eq(response));
    }

    @Test
    void explainsWhenTicketIsUnavailableBecauseReservationWasCanceled() throws Exception {
        UUID publicId = UUID.randomUUID();
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        when(reservaRepository.findByPublicId(publicId))
                .thenReturn(Optional.of(new Reserva().setEstadoReserva("CANCELADA")));

        controller.publicTicket(publicId.toString(), response);

        verify(response).setStatus(410);
        verify(response).setContentType("text/plain");
        org.junit.jupiter.api.Assertions.assertTrue(body.toString().contains("reserva fue cancelada"));
        verifyNoInteractions(generadorReporte);
    }

    @Test
    void explainsWhenTicketIsUnavailableBecauseReservationExpired() throws Exception {
        UUID publicId = UUID.randomUUID();
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        when(reservaRepository.findByPublicId(publicId))
                .thenReturn(Optional.of(new Reserva().setEstadoReserva("EXPIRADA")));

        controller.publicTicket(publicId.toString(), response);

        verify(response).setStatus(410);
        org.junit.jupiter.api.Assertions.assertTrue(body.toString().contains("reserva expiró"));
        verifyNoInteractions(generadorReporte);
    }

    @Test
    void explainsWhenPublicIdDoesNotExist() throws Exception {
        UUID publicId = UUID.randomUUID();
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        when(reservaRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        controller.publicTicket(publicId.toString(), response);

        verify(response).setStatus(404);
        org.junit.jupiter.api.Assertions.assertTrue(body.toString().contains("No encontramos un ticket"));
        verifyNoInteractions(generadorReporte);
    }

    @Test
    void explainsWhenPublicIdIsMalformed() throws Exception {
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        controller.publicTicket("not-a-uuid", response);

        verify(response).setStatus(404);
        org.junit.jupiter.api.Assertions.assertTrue(body.toString().contains("no es válido"));
        verifyNoInteractions(reservaRepository, generadorReporte);
    }

    @Test
    void explainsWhenTicketGenerationFails() throws Exception {
        UUID publicId = UUID.randomUUID();
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        Reserva reserva = new Reserva()
                .setPublicId(publicId)
                .setUuid("reservation-uuid")
                .setTipoReserva("puntual")
                .setEstadoReserva("PAGADA")
                .setTarifa(new Tarifa().setTipoCobro("por_evento"));
        when(reservaRepository.findByPublicId(publicId)).thenReturn(Optional.of(reserva));
        when(reservaTarifaDetalleRepository.findByReservaUuid("reservation-uuid"))
                .thenReturn(Collections.emptyList());
        doThrow(new IllegalStateException("template failure"))
                .when(generadorReporte)
                .generarSqlReportePdfNoCache(eq("ticket"),
                        eq("classpath:reportes/ticketPuntualEven.jrxml"), any(), eq(response));

        controller.publicTicket(publicId.toString(), response);

        verify(response).setStatus(500);
        org.junit.jupiter.api.Assertions.assertTrue(body.toString().contains("No se pudo generar el ticket"));
    }
}
