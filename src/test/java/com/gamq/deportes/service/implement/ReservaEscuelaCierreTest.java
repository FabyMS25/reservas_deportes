package com.gamq.deportes.service.implement;

import com.gamq.deportes.controller.ReservaController;
import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.*;
import com.gamq.deportes.security.DeportesPrincipal;
import com.gamq.deportes.security.PermissionAuthorizationService;
import com.gamq.deportes.security.PermissionCatalog;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservaEscuelaCierreTest {
    private ReservaServiceImpl service;
    private ReservaRepository reservas;
    private ReservaAuditoriaRepository auditorias;
    private Reserva base;

    @BeforeEach void setup() {
        service = new ReservaServiceImpl();
        reservas = mock(ReservaRepository.class);
        auditorias = mock(ReservaAuditoriaRepository.class);
        ReflectionTestUtils.setField(service, "reservaRepository", reservas);
        ReflectionTestUtils.setField(service, "reservaAuditoriaRepository", auditorias);
        base = new Reserva().setUuid("base").setTipoReserva("recurrente").setEsReservaGratuita(true)
                .setOrganizacion(new Organizacion().setUuid("school").setTipoOrganizacion("ESCUELA_MUNICIPAL"))
                .setEstadoValidacion("APROBADA").setEstadoReserva("EN_CURSO").setNumeroReserva("R-1")
                .setCancelacionAutomatica(false).setNumeroTasa(null);
        base.setReservaRecurrente(new ReservaRecurrente().setFechaInicio(LocalDate.of(2026, 1, 1))
                .setFechaFin(LocalDate.of(2026, 12, 31)).setActivo(true));
        when(reservas.findByUuidForRevision("base")).thenReturn(Optional.of(base));
        when(reservas.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        authenticate("STAFF", true, true);
    }

    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    private void authenticate(String type, boolean admin, boolean update) {
        DeportesPrincipal principal = new DeportesPrincipal("actor", type, admin,
                update ? Set.of(PermissionCatalog.key("/reservations/recurrent", "ACTUALIZAR")) : Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
    }

    @ParameterizedTest @ValueSource(strings = {"PENDIENTE", "PAGADA", "EN_CURSO"})
    void closesOnlyBaseAndAuditsAuthenticatedActor(String state) {
        base.setEstadoReserva(state);
        ReservaDto result = service.completarReservaEscuela("base");
        assertEquals("COMPLETADA", result.getEstadoReserva());
        assertEquals("APROBADA", result.getEstadoValidacion());
        assertNull(result.getNumeroTasa());
        assertNull(result.getFechaLimitePago());
        assertEquals(LocalDate.of(2026, 12, 31), result.getReservaRecurrenteDto().getFechaFin());
        assertTrue(base.getReservaRecurrente().getActivo());
        ArgumentCaptor<ReservaAuditoria> captor = ArgumentCaptor.forClass(ReservaAuditoria.class);
        verify(auditorias).save(captor.capture());
        assertEquals("actor", captor.getValue().getUsuarioUuid());
        assertEquals(state, captor.getValue().getEstadoAnterior());
        assertEquals("COMPLETADA", captor.getValue().getEstadoNuevo());
        assertEquals("COMPLETE_SCHOOL", captor.getValue().getAccion());
        verify(reservas).save(base);
    }

    @Test void repeatedClosureIsIdempotent() {
        service.completarReservaEscuela("base");
        clearInvocations(reservas, auditorias);
        assertEquals("COMPLETADA", service.completarReservaEscuela("base").getEstadoReserva());
        verify(reservas, never()).save(any());
        verifyNoInteractions(auditorias);
    }

    @ParameterizedTest @ValueSource(strings = {"SOLICITADA", "CANCELADA", "EXPIRADA", "NO_SHOW"})
    void rejectsNonClosableStates(String state) {
        base.setEstadoReserva(state);
        assertThrows(IllegalStateException.class, () -> service.completarReservaEscuela("base"));
        assertEquals(state, base.getEstadoReserva());
        verify(reservas, never()).save(any());
        verifyNoInteractions(auditorias);
    }

    @ParameterizedTest @ValueSource(strings = {"PENDIENTE", "RECHAZADA", "VENCIDA"})
    void requiresAdministrativeApproval(String validation) {
        base.setEstadoValidacion(validation);
        assertThrows(IllegalStateException.class, () -> service.completarReservaEscuela("base"));
        verify(reservas, never()).save(any());
    }

    @ParameterizedTest @CsvSource({"puntual,true,ESCUELA_MUNICIPAL", "periodo,true,ESCUELA_MUNICIPAL",
            "recurrente,false,ESCUELA_MUNICIPAL", "recurrente,true,CLUB"})
    void rejectsWrongReservationKinds(String type, boolean free, String organization) {
        base.setTipoReserva(type).setEsReservaGratuita(free);
        base.getOrganizacion().setTipoOrganizacion(organization);
        assertThrows(IllegalArgumentException.class, () -> service.completarReservaEscuela("base"));
        verify(reservas, never()).save(any());
    }

    @Test void schoolFreeTextReasonCannotSubstituteForASchoolOrganization() {
        base.setOrganizacion(null).setMotivoGratuidad("ESCUELA_MUNICIPAL");
        assertThrows(IllegalArgumentException.class, () -> service.completarReservaEscuela("base"));
    }

    @ParameterizedTest @CsvSource({"STAFF,true,true,true", "STAFF,true,false,false",
            "TAXPAYER,false,true,true", "TAXPAYER,false,false,false"})
    void endpointUsesExistingUpdatePermissionAndScopeNotAdminOnly(String type, boolean admin, boolean update, boolean allowed) throws Exception {
        String expression = ReservaController.class.getMethod("completarReservaEscuela", String.class)
                .getAnnotation(PreAuthorize.class).value();
        assertEquals("@permissions.reservation('ACTUALIZAR',#p0)", expression);
        authenticate(type, admin, update);
        base.setCampoDeportivo(new CampoDeportivo().setUuid("field"));
        base.setContribuyente(new Contribuyente().setUuid("actor"));
        when(reservas.findByUuid("base")).thenReturn(Optional.of(base));
        PermissionAuthorizationService permissions = new PermissionAuthorizationService(reservas,
                mock(ReservaPuntualRepository.class), mock(ReservaRecurrenteRepository.class), mock(ReservaPeriodoRepository.class),
                mock(OrganizacionRepository.class), mock(EncargadoComplejoRepository.class), mock(CampoDeportivoRepository.class),
                mock(NotificacionRepository.class), mock(ReservaTarifaDetalleRepository.class), mock(PagoRepository.class));
        assertEquals(allowed, permissions.reservation("ACTUALIZAR", "base"));
        if ("TAXPAYER".equals(type)) {
            base.getContribuyente().setUuid("someone-else");
            assertFalse(permissions.reservation("ACTUALIZAR", "base"));
        }
    }
}
