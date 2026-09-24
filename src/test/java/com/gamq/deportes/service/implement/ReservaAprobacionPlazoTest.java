package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.model.ConfiguracionSistema;
import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.Tarifa;
import com.gamq.deportes.repository.ConfiguracionSistemaRepository;
import com.gamq.deportes.repository.ReservaAuditoriaRepository;
import com.gamq.deportes.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservaAprobacionPlazoTest {
    private ReservaServiceImpl service;
    private ReservaRepository reservas;
    private ConfiguracionSistemaRepository configs;
    private ReservaAuditoriaRepository auditorias;
    private final LocalDateTime fechaSolicitud = LocalDateTime.of(2026, 1, 1, 9, 30);

    @BeforeEach void setup() {
        service = new ReservaServiceImpl();
        reservas = mock(ReservaRepository.class);
        configs = mock(ConfiguracionSistemaRepository.class);
        auditorias = mock(ReservaAuditoriaRepository.class);
        ReflectionTestUtils.setField(service, "reservaRepository", reservas);
        ReflectionTestUtils.setField(service, "configuracionSistemaRepository", configs);
        ReflectionTestUtils.setField(service, "reservaAuditoriaRepository", auditorias);
        when(reservas.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Reserva solicitud(String tipo) {
        Reserva reserva = new Reserva().setUuid("reserva").setTipoReserva(tipo)
                .setNumeroReserva("R-0001/26").setNumeroTasa("42538")
                .setEstadoReserva("SOLICITADA").setEstadoValidacion("PENDIENTE")
                .setCancelacionAutomatica(true).setFechaCreacion(fechaSolicitud)
                .setFechaLimitePago(fechaSolicitud.plusHours(1));
        when(reservas.findByUuidForRevision("reserva")).thenReturn(Optional.of(reserva));
        return reserva;
    }

    private void config(String clave, double horas) {
        when(configs.findByClaveAndEstadoFalse(clave)).thenReturn(Optional.of(
                new ConfiguracionSistema().setActivo(true).setValorNumerico(horas)));
    }

    @ParameterizedTest @CsvSource({
            "puntual,por_hora,HORAS_VALIDACION_PAGO,4",
            "puntual,por_evento,HORAS_VALIDACION_PAGO_EVENTOS,8",
            "recurrente,por_sesion,HORAS_VALIDACION_PAGO_RECURRENTES,24",
            "recurrente,por_evento,HORAS_VALIDACION_PAGO_RECURRENTES,36"
    })
    void replacesOldDeadlineWithConfiguredHoursFromApproval(String tipo, String cobro, String clave, int horas) {
        Reserva reserva = solicitud(tipo).setTarifa(new Tarifa().setTipoCobro(cobro));
        config(clave, horas);
        LocalDateTime before = LocalDateTime.now(ZoneId.of("America/La_Paz"));

        ReservaDto result = service.aprobarReserva("reserva", "admin");

        LocalDateTime after = LocalDateTime.now(ZoneId.of("America/La_Paz"));
        assertFalse(result.getFechaAprobacion().isBefore(before));
        assertFalse(result.getFechaAprobacion().isAfter(after));
        assertEquals(result.getFechaAprobacion().plusHours(horas), result.getFechaLimitePago());
        assertEquals(result.getFechaLimitePago(), reserva.getFechaLimitePago());
        assertEquals(fechaSolicitud, reserva.getFechaCreacion());
        assertEquals("APROBADA", result.getEstadoValidacion());
        assertEquals("PENDIENTE", result.getEstadoReserva());
        assertEquals("42538", result.getNumeroTasa());
        verify(configs).findByClaveAndEstadoFalse(clave);
        verify(reservas).save(reserva);
        verify(auditorias).save(any());
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente"})
    void replacesFutureRequestDeadlinesToo(String tipo) {
        Reserva reserva = solicitud(tipo).setFechaLimitePago(LocalDateTime.of(2099, 12, 31, 23, 59));
        config("HORAS_VALIDACION_PAGO", 6);
        ReservaDto result = service.aprobarReserva("reserva", "admin");
        assertEquals(result.getFechaAprobacion().plusHours(6), reserva.getFechaLimitePago());
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente"})
    void freeBookingsNeverReceiveAPaymentDeadline(String tipo) {
        Reserva reserva = solicitud(tipo).setEsReservaGratuita(true).setNumeroTasa(null);
        ReservaDto result = service.aprobarReserva("reserva", "admin");
        assertNull(result.getFechaLimitePago());
        assertFalse(result.getCancelacionAutomatica());
        assertEquals("APROBADA", result.getEstadoValidacion());
        verifyNoInteractions(configs);
        verify(reservas).save(reserva);
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente"})
    void automaticCancellationDisabledKeepsDeadlineNull(String tipo) {
        solicitud(tipo).setCancelacionAutomatica(false).setMotivoCancelacionAutomatica("Postpago autorizado");
        ReservaDto result = service.aprobarReserva("reserva", "admin");
        assertNull(result.getFechaLimitePago());
        assertFalse(result.getCancelacionAutomatica());
        assertEquals("Postpago autorizado", result.getMotivoCancelacionAutomatica());
        verifyNoInteractions(configs);
    }

    @ParameterizedTest @CsvSource({"puntual,", "recurrente,'   '"})
    void cannotApproveChargeableRequestWithoutRegisteredRate(String tipo, String numeroTasa) {
        Reserva reserva = solicitud(tipo).setNumeroTasa(numeroTasa);
        LocalDateTime oldDeadline = reserva.getFechaLimitePago();
        assertThrows(IllegalArgumentException.class, () -> service.aprobarReserva("reserva", "admin"));
        assertEquals("SOLICITADA", reserva.getEstadoReserva());
        assertEquals("PENDIENTE", reserva.getEstadoValidacion());
        assertEquals(oldDeadline, reserva.getFechaLimitePago());
        assertNull(reserva.getFechaAprobacion());
        verify(reservas, never()).save(any());
        verifyNoInteractions(configs, auditorias);
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente"})
    void repeatedApprovalDoesNotExtendDeadlineOrChangeConfirmedUseState(String tipo) {
        Reserva reserva = solicitud(tipo);
        config("HORAS_VALIDACION_PAGO", 4);
        ReservaDto first = service.aprobarReserva("reserva", "admin");
        reserva.setEstadoReserva("EN_CURSO");
        clearInvocations(reservas, configs, auditorias);
        ReservaDto repeated = service.aprobarReserva("reserva", "other-admin");
        assertEquals(first.getFechaLimitePago(), repeated.getFechaLimitePago());
        assertEquals(first.getFechaAprobacion(), repeated.getFechaAprobacion());
        assertEquals("admin", repeated.getAprobadoPor());
        assertEquals("EN_CURSO", repeated.getEstadoReserva());
        verify(reservas, never()).save(any());
        verifyNoInteractions(configs, auditorias);
    }

    @ParameterizedTest @ValueSource(strings = {"CANCELADA", "EXPIRADA", "COMPLETADA", "NO_SHOW"})
    void approvalCannotStartANewDeadlineForANonRequestedBooking(String estado) {
        Reserva reserva = solicitud("recurrente").setEstadoReserva(estado);
        assertThrows(IllegalArgumentException.class, () -> service.aprobarReserva("reserva", "admin"));
        assertEquals(estado, reserva.getEstadoReserva());
        verify(reservas, never()).save(any());
    }

    @Test void monthlyApprovalIsOutsideThisDeadlineChange() {
        solicitud("periodo").setCancelacionAutomatica(false).setFechaLimitePago(null);
        ReservaDto result = service.aprobarReserva("reserva", "admin");
        assertNull(result.getFechaLimitePago());
        assertFalse(result.getCancelacionAutomatica());
        verifyNoInteractions(configs);
    }

    @Test void inactiveSpecificConfigurationFallsBackToExistingGeneralPolicy() {
        solicitud("recurrente");
        when(configs.findByClaveAndEstadoFalse("HORAS_VALIDACION_PAGO_RECURRENTES"))
                .thenReturn(Optional.of(new ConfiguracionSistema().setActivo(false).setValorNumerico(100d)));
        config("HORAS_VALIDACION_PAGO", 7);
        ReservaDto result = service.aprobarReserva("reserva", "admin");
        assertEquals(result.getFechaAprobacion().plusHours(7), result.getFechaLimitePago());
    }
}
