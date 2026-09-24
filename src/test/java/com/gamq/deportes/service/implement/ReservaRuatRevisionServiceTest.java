package com.gamq.deportes.service.implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamq.deportes.dto.*;
import com.gamq.deportes.dto.ReservaRevisionPage.Resultado;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.*;
import com.gamq.deportes.security.ReservationListScope;
import com.gamq.deportes.service.ApiService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.*;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.client.ResourceAccessException;

import javax.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReservaRuatRevisionServiceTest {
    private final LocalDateTime now = LocalDateTime.of(2026, 9, 21, 12, 0); // Monday, Bolivia
    private ApiService ruat;
    private ReservaRepository reservas;
    private PagoRepository pagos;
    private ReservaAuditoriaRepository auditorias;
    private ReservaNotificacionService avisos;
    private PagoNotificacionService pagoAvisos;
    private EntityManager em;
    private PlatformTransactionManager tm;
    private ReservaRuatRevisionService reviewer;

    @BeforeEach void setup() {
        ruat = mock(ApiService.class);
        reservas = mock(ReservaRepository.class);
        pagos = mock(PagoRepository.class);
        auditorias = mock(ReservaAuditoriaRepository.class);
        avisos = mock(ReservaNotificacionService.class);
        pagoAvisos = mock(PagoNotificacionService.class);
        em = mock(EntityManager.class);
        tm = mock(PlatformTransactionManager.class);
        when(tm.getTransaction(any())).thenAnswer(i -> new SimpleTransactionStatus());
        when(pagos.save(any(Pago.class))).thenAnswer(i -> ((Pago)i.getArgument(0)).setUuid("payment"));
        when(reservas.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        reviewer = new ReservaRuatRevisionService(ruat, new ObjectMapper(), reservas, pagos,
                auditorias, avisos, pagoAvisos, em, tm,
                Clock.fixed(now.atZone(ZoneId.of("America/La_Paz")).toInstant(), ZoneOffset.UTC));
    }

    private Reserva booking(String type, String state) {
        Reserva r = new Reserva().setIdReserva(1L).setUuid("reservation").setNumeroReserva("R-1").setNumeroTasa("42538")
                .setTipoReserva(type).setEstadoReserva(state).setEstadoValidacion("APROBADA")
                .setCancelacionAutomatica(true).setFechaLimitePago(now.minusDays(1));
        if ("recurrente".equals(type)) r.setReservaRecurrente(new ReservaRecurrente().setReserva(r)
                .setFechaInicio(LocalDate.of(2026, 9, 1)).setFechaFin(LocalDate.of(2026, 9, 30))
                .setDiasSemana("lunes, miércoles"));
        if ("puntual".equals(type)) r.setReservaPuntual(new ReservaPuntual().setReserva(r)
                .setFechaUso(now.toLocalDate()).setHoraInicio(LocalTime.of(11, 0)).setHoraFin(LocalTime.of(13, 0)));
        if ("periodo".equals(type)) r.getReservasPeriodoList().add(new ReservaPeriodo().setReserva(r).setAño(2026).setMes(8));
        return r;
    }
    private ResponseEntity<byte[]> response(String json) {
        return ResponseEntity.ok(json.getBytes(StandardCharsets.UTF_8));
    }
    private void unpaid() {
        when(ruat.consultaPagoRevision(any())).thenReturn(response("{\"continuarFlujo\":true,\"datos\":{\"pagoTasa\":null}}"));
    }
    private void paid() {
        when(ruat.consultaPagoRevision(any())).thenReturn(response("{\"continuarFlujo\":true,\"datos\":{\"pagoTasa\":{" +
                "\"numeroTasa\":\"42538\",\"folio\":\"2870117315874\",\"fechaPago\":\"17/09/2026\"," +
                "\"montoPago\":2560,\"formaPago\":\"PAGO ORIGINAL TOTAL\",\"entidadFinanciera\":\"BANCO UNION S.A.\"}}}"));
    }
    private void noWrites() {
        verify(reservas, never()).save(any());
        verify(pagos, never()).save(any());
        verify(auditorias, never()).save(any());
        verifyNoInteractions(avisos, pagoAvisos);
    }

    @ParameterizedTest @CsvSource({
        "puntual,CANCELADA", "puntual,NO_SHOW", "puntual,COMPLETADA", "puntual,EXPIRADA",
        "recurrente,CANCELADA", "recurrente,NO_SHOW", "recurrente,COMPLETADA", "recurrente,EXPIRADA",
        "periodo,CANCELADA", "periodo,NO_SHOW", "periodo,COMPLETADA", "periodo,EXPIRADA"
    })
    void terminalsAreUntouched(String type, String state) {
        assertEquals(state, reviewer.revisarEstado(booking(type, state)).getReservaDto().getEstadoReserva());
        verifyNoInteractions(ruat);
        noWrites();
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente", "periodo"})
    void consultsPaidPendingEvenWithoutAutomaticCancellationOrDeadline(String type) {
        paid();
        Reserva r = booking(type, "PENDIENTE").setCancelacionAutomatica(false).setFechaLimitePago(null);
        assertEquals("periodo".equals(type) ? "PAGADA" : "EN_CURSO", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        verify(ruat, never()).anularTasaRevision(any());
        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagos).save(captor.capture());
        assertEquals("PAGADO", captor.getValue().getEstadoPago());
        assertEquals(LocalDate.of(2026, 9, 17), captor.getValue().getFechaPago().toInstant()
                .atZone(ZoneId.of("America/La_Paz")).toLocalDate());
    }

    @Test void paidOn17WithDeadline18NeverAnnulsEvenWhenReviewedLater() {
        paid();
        Reserva r = booking("recurrente", "PENDIENTE").setFechaLimitePago(LocalDateTime.of(2026, 9, 18, 12, 0));
        assertEquals("EN_CURSO", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        verify(ruat, never()).anularTasaRevision(any());
        var order = inOrder(ruat, pagos, reservas);
        order.verify(ruat).consultaPagoRevision(any());
        order.verify(pagos).save(any());
        order.verify(reservas).save(r);
    }

    @ParameterizedTest @ValueSource(strings = {"PENDIENTE", "PAGADA", "EN_CURSO"})
    void freeSchoolBaseDoesNotCompleteAutomaticallyAfterItsEndDate(String state) {
        Reserva reserva = booking("recurrente", state).setEsReservaGratuita(true).setNumeroTasa(null)
                .setOrganizacion(new Organizacion().setUuid("school").setTipoOrganizacion("ESCUELA_MUNICIPAL"));
        reserva.getReservaRecurrente().setFechaFin(now.toLocalDate().minusDays(1));
        Resultado result = reviewer.revisarEstado(reserva);
        assertEquals(state, result.getReservaDto().getEstadoReserva());
        assertEquals("SIN_CAMBIOS", result.getResultado());
        verifyNoInteractions(ruat);
        noWrites();
    }

    @Test void freeSchoolBaseStillEntersUseInsideItsOriginalDateRange() {
        Reserva reserva = booking("recurrente", "PENDIENTE").setEsReservaGratuita(true).setNumeroTasa(null)
                .setOrganizacion(new Organizacion().setUuid("school").setTipoOrganizacion("ESCUELA_MUNICIPAL"));
        assertEquals("EN_CURSO", reviewer.revisarEstado(reserva).getReservaDto().getEstadoReserva());
        assertEquals(LocalDate.of(2026, 9, 30), reserva.getReservaRecurrente().getFechaFin());
        verifyNoInteractions(ruat);
    }

    @ParameterizedTest @CsvSource({"recurrente,true,CLUB", "recurrente,false,ESCUELA_MUNICIPAL", "puntual,true,ESCUELA_MUNICIPAL"})
    void manualSchoolClosureExceptionDoesNotChangeOtherBookingRules(String type, boolean free, String organization) {
        Reserva reserva = booking(type, "EN_CURSO").setEsReservaGratuita(free)
                .setOrganizacion(new Organizacion().setUuid("org").setTipoOrganizacion(organization));
        if ("recurrente".equals(type)) reserva.getReservaRecurrente().setFechaFin(now.toLocalDate().minusDays(1));
        else reserva.getReservaPuntual().setFechaUso(now.toLocalDate().minusDays(1));
        assertEquals("COMPLETADA", reviewer.revisarEstado(reserva).getReservaDto().getEstadoReserva());
        verifyNoInteractions(ruat);
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente"})
    void expirationRequiresOrderedSuccessfulQueryAndAnnulment(String type) {
        unpaid();
        when(ruat.anularTasaRevision(any())).thenReturn(response("{\"continuarFlujo\":true}"));
        Reserva r = booking(type, "PENDIENTE");
        assertEquals("EXPIRADA", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        var order = inOrder(ruat, reservas);
        order.verify(ruat).consultaPagoRevision(any());
        order.verify(ruat).anularTasaRevision(any());
        order.verify(reservas).save(r);
        verify(avisos).notificarExpirada(r);
    }

    @ParameterizedTest @ValueSource(strings = {
        "{}", "{\"continuarFlujo\":false}", "{\"continuarFlujo\":true,\"datos\":[]}", "not-json",
        "{\"continuarFlujo\":true,\"datos\":{\"pagoTasa\":{\"numeroTasa\":\"DIFFERENT\"}}}"
    })
    void invalidQueryCannotCauseAnyLocalMutationOrAnnulment(String body) {
        when(ruat.consultaPagoRevision(any())).thenReturn(response(body));
        Reserva r = booking("recurrente", "PENDIENTE");
        assertEquals("ERROR", reviewer.revisarEstado(r).getResultado());
        assertEquals("PENDIENTE", r.getEstadoReserva());
        verify(ruat, never()).anularTasaRevision(any());
        noWrites();
    }

    @ParameterizedTest @ValueSource(strings = {"{}", "{\"continuarFlujo\":false}", "not-json"})
    void http200DoesNotProveAnnulment(String body) {
        unpaid();
        when(ruat.anularTasaRevision(any())).thenReturn(response(body));
        Reserva r = booking("puntual", "PENDIENTE");
        assertEquals("ERROR", reviewer.revisarEstado(r).getResultado());
        assertEquals("PENDIENTE", r.getEstadoReserva());
        assertNull(r.getFechaRechazo());
        noWrites();
    }

    @Test void annulTimeoutDoesNotExpireOrRetry() {
        unpaid();
        when(ruat.anularTasaRevision(any())).thenThrow(new ResourceAccessException("timeout"));
        assertEquals("ERROR", reviewer.revisarEstado(booking("puntual", "PENDIENTE")).getResultado());
        verify(ruat, times(1)).anularTasaRevision(any());
        noWrites();
    }

    @ParameterizedTest @ValueSource(ints = {0, 1, 24})
    void deadlineIsStrictAndUsesBoliviaNotUtc(int hours) {
        unpaid();
        reviewer.revisarEstado(booking("recurrente", "PENDIENTE").setFechaLimitePago(now.plusHours(hours)));
        verify(ruat).consultaPagoRevision(any());
        verify(ruat, never()).anularTasaRevision(any());
        noWrites();
    }

    @Test void existingLocalPaymentPreventsAnnulmentOnRemoteDisagreement() {
        unpaid();
        Reserva r = booking("puntual", "PENDIENTE");
        r.getPagosList().add(new Pago().setEstadoPago("PAGADO"));
        reviewer.revisarEstado(r);
        verify(ruat, never()).anularTasaRevision(any());
        noWrites();
    }

    @Test void paymentIsNotDuplicated() {
        paid();
        when(pagos.findByFolioAndReservaUuid(anyString(), anyString())).thenReturn(Optional.of(new Pago()));
        reviewer.revisarEstado(booking("periodo", "PENDIENTE"));
        verify(pagos, never()).save(any());
        verify(reservas).save(any());
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente"})
    void futurePaidUseIsPagadaAndPastPaidUseIsCompleted(String type) {
        paid();
        Reserva future = booking(type, "PENDIENTE");
        Reserva past = booking(type, "PENDIENTE");
        if ("puntual".equals(type)) {
            future.getReservaPuntual().setFechaUso(now.toLocalDate().plusDays(1));
            past.getReservaPuntual().setFechaUso(now.toLocalDate().minusDays(1));
        } else {
            future.getReservaRecurrente().setFechaInicio(now.toLocalDate().plusDays(1));
            past.getReservaRecurrente().setFechaFin(now.toLocalDate().minusDays(1));
        }
        assertEquals("PAGADA", reviewer.revisarEstado(future).getReservaDto().getEstadoReserva());
        assertEquals("COMPLETADA", reviewer.revisarEstado(past).getReservaDto().getEstadoReserva());
        verify(ruat, never()).anularTasaRevision(any());
    }

    @Test void recurringEndDateIsInclusiveAndDaysAreHonoredWithoutRegression() {
        Reserva r = booking("recurrente", "PAGADA");
        r.getReservaRecurrente().setFechaFin(now.toLocalDate());
        assertEquals("EN_CURSO", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        r.getReservaRecurrente().setDiasSemana("martes");
        assertEquals("EN_CURSO", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        r.setEstadoReserva("PAGADA");
        assertEquals("PAGADA", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        r.getReservaRecurrente().setFechaFin(now.toLocalDate().minusDays(1));
        assertEquals("COMPLETADA", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        verifyNoInteractions(ruat);
    }

    @Test void punctualStartEndAreInclusiveButOneSecondAfterEndCompletes() {
        Reserva r = booking("puntual", "PAGADA");
        r.getReservaPuntual().setHoraInicio(now.toLocalTime()).setHoraFin(now.toLocalTime().plusHours(1));
        assertEquals("EN_CURSO", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        r.getReservaPuntual().setHoraInicio(now.toLocalTime().minusHours(1)).setHoraFin(now.toLocalTime());
        assertEquals("EN_CURSO", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        r.getReservaPuntual().setHoraFin(now.toLocalTime().minusSeconds(1));
        assertEquals("COMPLETADA", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        verifyNoInteractions(ruat);
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente"})
    void approvedFreeUseProgressesWithoutRuatButUnapprovedDoesNot(String type) {
        Reserva r = booking(type, "PENDIENTE").setEsReservaGratuita(true).setEstadoValidacion("PENDIENTE");
        assertEquals("PENDIENTE", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        r.setEstadoValidacion("APROBADA");
        assertEquals("EN_CURSO", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        verifyNoInteractions(ruat);
    }

    @Test void rejectionCannotCancelAnIssuedTaxLocally() {
        Reserva r = booking("puntual", "SOLICITADA").setEstadoValidacion("RECHAZADA");
        assertEquals("SOLICITADA", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        noWrites();
        r.setNumeroTasa(null);
        assertEquals("CANCELADA", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
    }

    @Test void mensualidadNeverExpiresEvenWithInvalidLegacyFlagAndDeadline() {
        unpaid();
        Reserva r = booking("periodo", "PENDIENTE"); // deliberately legacy true + expired deadline
        assertEquals("PENDIENTE", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        verify(ruat).consultaPagoRevision(any());
        verify(ruat, never()).anularTasaRevision(any());
        noWrites();
    }

    @Test void mensualidadUsesOriginalTwoStepFlowAndNeverEnCurso() {
        paid();
        Reserva r = booking("periodo", "PENDIENTE"); // August
        assertEquals("PAGADA", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        assertEquals("COMPLETADA", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        verify(ruat, times(1)).consultaPagoRevision(any());
        verify(ruat, never()).anularTasaRevision(any());
    }

    @ParameterizedTest @CsvSource({"2026,9", "2026,10", "2027,1"})
    void currentOrFutureMensualidadStaysPaid(int year, int month) {
        Reserva r = booking("periodo", "PAGADA");
        r.getReservasPeriodoList().get(0).setAño(year).setMes(month);
        assertEquals("PAGADA", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        verifyNoInteractions(ruat);
        noWrites();
    }

    @Test void mensualidadPolicyForcesFalseAndNullWithoutRequiringAnExceptionReason() {
        ReservaServiceImpl service = new ReservaServiceImpl();
        Reserva r = new Reserva().setTipoReserva("periodo")
                .setCancelacionAutomatica(true).setFechaLimitePago(now.minusDays(1));
        ReflectionTestUtils.invokeMethod(service, "aplicarPoliticaPago", r);
        assertFalse(r.getCancelacionAutomatica());
        assertNull(r.getFechaLimitePago());
    }

    @Test void recurrentDeadlineUsesConfigured24HoursAndDoesNotRecalculateAnExistingDeadline() {
        ReservaServiceImpl service = new ReservaServiceImpl();
        ConfiguracionSistemaRepository configs = mock(ConfiguracionSistemaRepository.class);
        ConfiguracionSistema setting = new ConfiguracionSistema().setActivo(true).setValorNumerico(24.0);
        when(configs.findByClaveAndEstadoFalse("HORAS_VALIDACION_PAGO_RECURRENTES")).thenReturn(Optional.of(setting));
        ReflectionTestUtils.setField(service, "configuracionSistemaRepository", configs);
        Reserva r = new Reserva().setTipoReserva("recurrente").setCancelacionAutomatica(true);
        LocalDateTime before = LocalDateTime.now(ZoneId.of("America/La_Paz")).plusHours(24);
        ReflectionTestUtils.invokeMethod(service, "aplicarPoliticaPago", r);
        LocalDateTime after = LocalDateTime.now(ZoneId.of("America/La_Paz")).plusHours(24);
        assertFalse(r.getFechaLimitePago().isBefore(before));
        assertFalse(r.getFechaLimitePago().isAfter(after));
        r.setFechaLimitePago(LocalDateTime.of(2026, 9, 18, 12, 0));
        ReflectionTestUtils.invokeMethod(service, "aplicarPoliticaPago", r);
        assertEquals(LocalDateTime.of(2026, 9, 18, 12, 0), r.getFechaLimitePago());
    }

    @Test void paidBeforeFutureDeadlineIsNotAnnulled() {
        paid();
        Reserva r = booking("recurrente", "PENDIENTE").setFechaLimitePago(now.plusDays(1));
        assertEquals("EN_CURSO", reviewer.revisarEstado(r).getReservaDto().getEstadoReserva());
        verify(ruat, never()).anularTasaRevision(any());
    }

    @Test void missingDeadlineAndFalseFlagStillQueryUnpaidButNeverAnnul() {
        unpaid();
        reviewer.revisarEstado(booking("recurrente", "PENDIENTE").setFechaLimitePago(null));
        reviewer.revisarEstado(booking("puntual", "PENDIENTE").setCancelacionAutomatica(false));
        verify(ruat, times(2)).consultaPagoRevision(any());
        verify(ruat, never()).anularTasaRevision(any());
        noWrites();
    }

    @Test void httpFailureAndEmptyBodyDoNotChangeState() {
        when(ruat.consultaPagoRevision(any())).thenReturn(ResponseEntity.status(503).body(new byte[0]));
        assertEquals("ERROR", reviewer.revisarEstado(booking("periodo", "PENDIENTE")).getResultado());
        when(ruat.consultaPagoRevision(any())).thenReturn(ResponseEntity.ok().build());
        assertEquals("ERROR", reviewer.revisarEstado(booking("recurrente", "PENDIENTE")).getResultado());
        noWrites();
    }

    @Test void failureReturnsTheListAndReportsSkippedRowsWithoutInventingChanges() throws Exception {
        when(ruat.consultaPagoRevision(any())).thenThrow(new ResourceAccessException("connection refused"));
        Reserva a = booking("recurrente", "PENDIENTE"), b = booking("periodo", "PENDIENTE").setUuid("second");
        when(reservas.findByUuidForRevision(a.getUuid())).thenReturn(Optional.of(a));
        Page<Reserva> source = new PageImpl<>(List.of(a, b), PageRequest.of(0, 5), 2);
        ReservaRevisionPage<String> result = (ReservaRevisionPage<String>)
                reviewer.revisarPagina(() -> source, r -> r, Reserva::getEstadoReserva);
        assertEquals(List.of("PENDIENTE", "PENDIENTE"), result.getContent());
        assertEquals(1, result.getRevision().getFallidas());
        assertEquals(1, result.getRevision().getOmitidas());
        assertEquals(0, result.getRevision().getActualizadas());
        assertFalse(result.getRevision().isCompleta());
        assertTrue(new ObjectMapper().findAndRegisterModules().writeValueAsString(result).contains("\"revision\""));
        verify(ruat, times(1)).consultaPagoRevision(any());
        noWrites();
    }

    @Test void changedRowsLeavingTheTabRemainInTheReportAndThePageIsRefilled() {
        paid();
        Reserva a = booking("periodo", "PENDIENTE");
        when(reservas.findByUuidForRevision(a.getUuid())).thenReturn(Optional.of(a));
        Supplier<Page<Reserva>> query = () -> "PENDIENTE".equals(a.getEstadoReserva())
                ? new PageImpl<>(List.of(a), PageRequest.of(0, 5), 1)
                : new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);
        ReservaRevisionPage<String> result = (ReservaRevisionPage<String>)
                reviewer.revisarPagina(query, r -> r, Reserva::getEstadoReserva);
        assertEquals(0, result.getTotalElements());
        assertEquals(1, result.getRevision().getActualizadas());
        Resultado change = result.getRevision().getResultados().get(0);
        assertEquals("42538", change.getReservaDto().getNumeroTasa());
        assertEquals("PENDIENTE", change.getEstadoAnterior());
        assertEquals("PAGADA", change.getReservaDto().getEstadoReserva());
        assertNotNull(change.getRespuestaRuat());
        assertNull(change.getMotivo()); // No invented message when upstream sent none.
    }

    @Test void localSaveFailureRollsBackTheRowAndDoesNotReportSuccess() {
        paid();
        Reserva r = booking("recurrente", "PENDIENTE");
        when(reservas.findByUuidForRevision(r.getUuid())).thenReturn(Optional.of(r));
        when(pagos.save(any())).thenThrow(new IllegalStateException("database unavailable"));
        ReservaRevisionPage<String> result = (ReservaRevisionPage<String>) reviewer.revisarPagina(
                () -> new PageImpl<>(List.of(r)), item -> item, Reserva::getEstadoReserva);
        verify(tm).rollback(any());
        assertEquals(1, result.getRevision().getFallidas());
        assertEquals(0, result.getRevision().getActualizadas());
        assertEquals("PENDIENTE", r.getEstadoReserva());
        verify(reservas, never()).save(any());
    }

    @Test void schedulerIsRealCodeButHasNoAutomaticTrigger() throws Exception {
        assertFalse(ReservaExpiracionScheduler.class.getMethod("procesarExpiraciones").isAnnotationPresent(Scheduled.class));
    }

    @Test @SuppressWarnings({"rawtypes", "unchecked"})
    void disabledSchedulerAdvancesByReviewedRowsNotTheRefilledPage() {
        ReservaRuatRevisionService review = mock(ReservaRuatRevisionService.class);
        ReservaRevisionPage.Resumen summary = new ReservaRevisionPage.Resumen();
        summary.getResultados().add(new Resultado().setReservaDto(new ReservaDto().setIdReserva(20L)));
        // Rows 1-20 completed; the refreshed page now contains 21-40, not reviewed yet.
        Page<Long> refilled = new PageImpl<>(List.of(21L, 40L));
        when(review.revisarPagina(any(), any(), any())).thenReturn((Page) new ReservaRevisionPage<>(refilled, summary))
                .thenReturn((Page) new ReservaRevisionPage<>(Page.empty(), new ReservaRevisionPage.Resumen()));
        ReservaExpiracionScheduler scheduler = new ReservaExpiracionScheduler(reservas, review);
        scheduler.procesarExpiraciones();
        assertEquals(20L, ReflectionTestUtils.getField(scheduler, "lastId"));
        scheduler.procesarExpiraciones();
        assertEquals(0L, ReflectionTestUtils.getField(scheduler, "lastId"));
    }

    @ParameterizedTest @CsvSource({"puntual,true", "recurrente,true", "puntual,false", "recurrente,false"})
    void unusedAdministrativeExpiryCannotCancelAnIssuedTax(String type, boolean hasTax) {
        ReservaPuntualRepository punctual = mock(ReservaPuntualRepository.class);
        ReservaRecurrenteRepository recurrent = mock(ReservaRecurrenteRepository.class);
        Reserva r = booking(type, "SOLICITADA").setEstadoValidacion("PENDIENTE");
        if (!hasTax) r.setNumeroTasa(null);
        if ("puntual".equals(type)) when(punctual.findSolicitudesVencidas(any(), any()))
                .thenReturn(List.of(r.getReservaPuntual()));
        else when(recurrent.findSolicitudesVencidas(any())).thenReturn(List.of(r.getReservaRecurrente()));
        new ReservaValidacionExpiracionService(punctual, recurrent, reservas, auditorias, avisos).procesar();
        assertEquals(hasTax ? "SOLICITADA" : "CANCELADA", r.getEstadoReserva());
        assertEquals(hasTax ? "PENDIENTE" : "VENCIDA", r.getEstadoValidacion());
        if (hasTax) noWrites();
        else verify(reservas).save(r);
    }

    @ParameterizedTest @ValueSource(ints = {404, 503})
    void remoteFailureDoesNotSkipTerminalRowsOrCalendarOnlyMonthlyProgress(int status) {
        when(ruat.consultaPagoRevision(any())).thenReturn(ResponseEntity.status(status)
                .body("{\"continuarFlujo\":false,\"mensaje\":\"No Existe el registro de la tasa.\"}".getBytes(StandardCharsets.UTF_8)));
        List<Reserva> rows = List.of(
                booking("periodo", "PENDIENTE").setUuid("pending").setNumeroReserva("E-0001/26"),
                booking("periodo", "COMPLETADA").setUuid("completed").setNumeroReserva("E-0002/26"),
                booking("periodo", "CANCELADA").setUuid("cancelled").setNumeroReserva("E-0003/26"),
                booking("periodo", "PAGADA").setUuid("paid").setNumeroReserva("E-0004/26"),
                booking("periodo", "PENDIENTE").setUuid("another").setNumeroReserva("E-0005/26"));
        rows.forEach(r -> when(reservas.findByUuidForRevision(r.getUuid())).thenReturn(Optional.of(r)));
        ReservaRevisionPage<ReservaDto> page = (ReservaRevisionPage<ReservaDto>) reviewer.revisarPagina(
                () -> new PageImpl<>(rows), r -> r, com.gamq.deportes.dto.mapper.ReservaMapper::toDto);
        assertEquals(5, page.getContent().size());
        assertEquals(5, page.getRevision().getResultados().size());
        assertEquals(status == 404 ? 2 : 1, page.getRevision().getFallidas());
        assertEquals(status == 404 ? 0 : 1, page.getRevision().getOmitidas());
        assertEquals(2, page.getRevision().getSinCambios());
        assertEquals(1, page.getRevision().getActualizadas());
        for (int i = 0; i < rows.size(); i++) {
            assertEquals(rows.get(i).getNumeroReserva(), page.getRevision().getResultados().get(i).getReservaDto().getNumeroReserva());
        }
        assertEquals("COMPLETADA", rows.get(3).getEstadoReserva());
        assertEquals(status == 404 ? "NO_CONFIRMADO" : "NO_DISPONIBLE", page.getRevision().getResultados().get(0).getRuat());
        verify(ruat, times(status == 404 ? 2 : 1)).consultaPagoRevision(any());
        verify(ruat, never()).anularTasaRevision(any());
        verify(pagos, never()).save(any());
    }

    @Test void failedLocalWriteReportKeepsTheOriginalDtoAndNumber() {
        paid();
        Reserva r = booking("recurrente", "PENDIENTE").setNumeroReserva("R-0042/26");
        when(reservas.findByUuidForRevision(r.getUuid())).thenReturn(Optional.of(r));
        when(pagos.save(any())).thenThrow(new IllegalStateException("write failed"));
        ReservaRevisionPage<ReservaDto> page = (ReservaRevisionPage<ReservaDto>) reviewer.revisarPagina(
                () -> new PageImpl<>(List.of(r)), item -> item, com.gamq.deportes.dto.mapper.ReservaMapper::toDto);
        Resultado result = page.getRevision().getResultados().get(0);
        assertEquals("ERROR", result.getResultado());
        assertEquals("R-0042/26", result.getReservaDto().getNumeroReserva());
        assertEquals("PENDIENTE", result.getReservaDto().getEstadoReserva());
        assertEquals("42538", result.getReservaDto().getNumeroTasa());
        assertNotNull(result.getRespuestaRuat());
        assertEquals(200, result.getEstadoHttpRuat());
        assertEquals("PAGO_CONFIRMADO", result.getRuat());
        assertNull(result.getMotivo()); // The upstream response had no message; never invent one.
        assertNotNull(result.getDetalleLocal());
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente", "periodo"})
    void genericPutCannotBypassRuatAndForceExpiration(String type) {
        ReservaServiceImpl service = new ReservaServiceImpl();
        ReflectionTestUtils.setField(service, "reservaRepository", reservas);
        Reserva r = booking(type, "PENDIENTE");
        when(reservas.findByUuid("reservation")).thenReturn(Optional.of(r));
        ReservaDto input = new ReservaDto().setUuid("reservation").setEstadoReserva("EXPIRADA");
        assertThrows(IllegalArgumentException.class, () -> service.actualizarReserva(input));
        assertEquals("PENDIENTE", r.getEstadoReserva());
        noWrites();
    }


    private static final String NO_PAYMENT_MESSAGE = "No Existe el registro de la tasa y otro ingreso.";
    private static final String NO_PAYMENTS_MADE_MESSAGE = "La Tasa y Otro Ingreso no tiene pagos realizados.";
    private ResponseEntity<byte[]> unpaidBusiness(int status) {
        return unpaidBusiness(status, NO_PAYMENT_MESSAGE, false);
    }

    private ResponseEntity<byte[]> unpaidBusiness(int status, String message, boolean wrapped) {
        String data = "\"mensaje\":\"" + message + "\",\"observacion\":null,\"pagoTasa\":null";
        return ResponseEntity.status(status).header("X-Response-Origin", "RUAT")
                .body(("{\"continuarFlujo\":false," + (wrapped ? "\"datos\":{" + data + "}" : data) + "}")
                        .getBytes(StandardCharsets.UTF_8));
    }

    static java.util.stream.Stream<Arguments> unpaidBusinessCases() {
        return java.util.stream.Stream.of("periodo", "recurrente", "puntual").flatMap(type ->
                java.util.stream.Stream.of(200, 404).flatMap(status ->
                        java.util.stream.Stream.of(NO_PAYMENT_MESSAGE, NO_PAYMENTS_MADE_MESSAGE).flatMap(message ->
                                java.util.stream.Stream.of(false, true).map(wrapped -> Arguments.of(type, status, message, wrapped)))));
    }

    @ParameterizedTest @MethodSource("unpaidBusinessCases")
    void actualUnpaidBusinessResponseIsNotAFailedConsultation(String type, int status, String message, boolean wrapped) throws Exception {
        ResponseEntity<byte[]> response = unpaidBusiness(status, message, wrapped);
        when(ruat.consultaPagoRevision(any())).thenReturn(response);
        Reserva r = booking(type, "PENDIENTE").setCancelacionAutomatica(false).setFechaLimitePago(null);
        Resultado result = reviewer.revisarEstado(r);
        assertEquals("SIN_CAMBIOS", result.getResultado());
        assertEquals("SIN_PAGO", result.getRuat());
        assertEquals(message, result.getMotivo());
        assertEquals(new ObjectMapper().readTree(response.getBody()), result.getRespuestaRuat());
        assertEquals(status, result.getEstadoHttpRuat());
        assertEquals("PENDIENTE", r.getEstadoReserva());
        assertNull(result.getDetalleLocal());
        noWrites();
        verify(ruat, never()).anularTasaRevision(any());
    }

    @ParameterizedTest @MethodSource("unpaidBusinessCases")
    void unpaidBusinessResponseChecksDeadlineThenRequiresConfirmedAnnulment(String type, int status, String message, boolean wrapped) {
        when(ruat.consultaPagoRevision(any())).thenReturn(unpaidBusiness(status, message, wrapped));
        when(ruat.anularTasaRevision(any())).thenReturn(response("{\"mensaje\":\"Tasa anulada.\",\"continuarFlujo\":true}"));
        Reserva r = booking(type, "PENDIENTE");
        Resultado result = reviewer.revisarEstado(r);
        if ("periodo".equals(type)) {
            assertEquals("PENDIENTE", r.getEstadoReserva());
            assertEquals(message, result.getMotivo());
            verify(ruat, never()).anularTasaRevision(any());
            noWrites();
            return;
        }
        assertEquals("EXPIRADA", r.getEstadoReserva());
        assertEquals("Tasa anulada.", result.getMotivo());
        var order = inOrder(ruat, reservas);
        order.verify(ruat).consultaPagoRevision(any());
        order.verify(ruat).anularTasaRevision(argThat(t ->
                "NO SE EFECTUO EL PAGO EN EL DIA DEL REGISTRO".equals(t.getAnular().getMotivoTasa())));
        order.verify(reservas).save(r);
    }

    @ParameterizedTest @MethodSource("unpaidBusinessCases")
    void unpaidBusinessDoesNotExpireMonthlyOrBookingsBeforeDeadline(String type, int status, String message, boolean wrapped) {
        when(ruat.consultaPagoRevision(any())).thenReturn(unpaidBusiness(status, message, wrapped));
        Reserva r = booking(type, "PENDIENTE").setFechaLimitePago(now.plusHours(1));
        assertEquals("SIN_CAMBIOS", reviewer.revisarEstado(r).getResultado());
        verify(ruat, never()).anularTasaRevision(any());
        noWrites();
    }

    @ParameterizedTest @ValueSource(strings = {NO_PAYMENT_MESSAGE, NO_PAYMENTS_MADE_MESSAGE})
    void unpaidResponseNeverCountsAsConfirmationOfAnAnnulment(String message) {
        when(ruat.consultaPagoRevision(any())).thenReturn(unpaidBusiness(404, message, false));
        when(ruat.anularTasaRevision(any())).thenReturn(unpaidBusiness(404, message, false));
        Reserva r = booking("recurrente", "PENDIENTE");
        Resultado result = reviewer.revisarEstado(r);
        assertEquals("ERROR", result.getResultado());
        assertEquals(message, result.getMotivo());
        assertEquals("PENDIENTE", r.getEstadoReserva());
        noWrites();
    }

    @ParameterizedTest @MethodSource("unpaidBusinessCases")
    void successfulUnpaidReviewReturnsACompleteReportWithNoFailures(String type, int status, String message, boolean wrapped) {
        when(ruat.consultaPagoRevision(any())).thenReturn(unpaidBusiness(status, message, wrapped));
        Reserva reserva = booking(type, "PENDIENTE").setFechaLimitePago(now.plusHours(1));
        when(reservas.findByUuidForRevision(reserva.getUuid())).thenReturn(Optional.of(reserva));
        ReservaRevisionPage<ReservaDto> result = (ReservaRevisionPage<ReservaDto>) reviewer.revisarLista(
                () -> new PageImpl<>(List.of(reserva)), r -> r, com.gamq.deportes.dto.mapper.ReservaMapper::toDto,
                cursor -> cursor == 0 ? new PageImpl<>(List.of(reserva)) : Page.empty());
        assertTrue(result.getRevision().isCompleta());
        assertEquals(1, result.getRevision().getSinCambios());
        assertEquals(0, result.getRevision().getFallidas());
        assertEquals(0, result.getRevision().getOmitidas());
        assertEquals(message, result.getRevision().getResultados().get(0).getMotivo());
        assertEquals("PENDIENTE", result.getContent().get(0).getEstadoReserva());
        verify(ruat, never()).anularTasaRevision(any());
        noWrites();
    }

    @ParameterizedTest @ValueSource(strings = {NO_PAYMENT_MESSAGE, NO_PAYMENTS_MADE_MESSAGE})
    void knownMessagesCannotHideTransportErrorsLocalErrorsOrContradictoryPaymentData(String message) {
        String body = "{\"continuarFlujo\":false,\"mensaje\":\"" + message + "\",\"pagoTasa\":null}";
        List<ResponseEntity<byte[]>> failures = new ArrayList<>();
        for (int status : List.of(400, 401, 403, 500, 502, 503)) {
            failures.add(ResponseEntity.status(status).header("X-Response-Origin", "RUAT").body(body.getBytes(StandardCharsets.UTF_8)));
        }
        for (String origin : List.of("ruat-be", "deportes-be")) {
            failures.add(ResponseEntity.status(404).header("X-Response-Origin", origin).body(body.getBytes(StandardCharsets.UTF_8)));
            failures.add(ResponseEntity.status(404).body(body.replace("\"pagoTasa\":null",
                    "\"pagoTasa\":null,\"detalle\":{\"origen\":\"" + origin + "\"}").getBytes(StandardCharsets.UTF_8)));
        }
        for (String replacement : List.of("\"pagoTasa\":{}", "\"pagoTasa\":false", "\"otroCampo\":null")) {
            failures.add(ResponseEntity.status(404).body(body.replace("\"pagoTasa\":null", replacement).getBytes(StandardCharsets.UTF_8)));
        }
        for (ResponseEntity<byte[]> failure : failures) {
            when(ruat.consultaPagoRevision(any())).thenReturn(failure);
            Reserva reserva = booking("recurrente", "PENDIENTE");
            Resultado result = reviewer.revisarEstado(reserva);
            assertEquals("ERROR", result.getResultado());
            assertEquals(message, result.getMotivo());
            assertEquals("PENDIENTE", reserva.getEstadoReserva());
        }
        verify(ruat, never()).anularTasaRevision(any());
        noWrites();
    }

    @ParameterizedTest @ValueSource(ints = {400, 401, 403, 404, 500, 502})
    void otherResponsesKeepTheirExactMessageAndNeverMeanUnpaid(int status) {
        String message = "  Mensaje original de RUAT: operación rechazada.  ";
        when(ruat.consultaPagoRevision(any())).thenReturn(ResponseEntity.status(status)
                .body(("{\"mensaje\":\"" + message + "\",\"continuarFlujo\":false,\"pagoTasa\":null}").getBytes(StandardCharsets.UTF_8)));
        Resultado result = reviewer.revisarEstado(booking("recurrente", "PENDIENTE"));
        assertEquals("ERROR", result.getResultado());
        assertEquals(message, result.getMotivo());
        assertEquals(status, result.getEstadoHttpRuat());
        verify(ruat, never()).anularTasaRevision(any());
        noWrites();
    }

    @Test void malformedOrContradictoryPaymentsCannotAuthorizeAnAnnulment() {
        for (String body : List.of(
                "{\"continuarFlujo\":true,\"mensaje\":\"Respuesta sin datos.\"}",
                "{\"continuarFlujo\":false,\"mensaje\":\"" + NO_PAYMENT_MESSAGE + "\"}",
                "{\"continuarFlujo\":false,\"mensaje\":\"" + NO_PAYMENT_MESSAGE + "\",\"pagoTasa\":{}}")) {
            when(ruat.consultaPagoRevision(any())).thenReturn(ResponseEntity.status(404).body(body.getBytes(StandardCharsets.UTF_8)));
            assertEquals("ERROR", reviewer.revisarEstado(booking("puntual", "PENDIENTE")).getResultado());
        }
        verify(ruat, never()).anularTasaRevision(any());
        noWrites();
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente", "periodo"})
    void fullReviewCrossesAllBatchesWhileReturningOnlyFiveRows(String type) {
        paid();
        List<Reserva> rows = new ArrayList<>();
        for (long id = 1; id <= 205; id++) {
            Reserva r = booking(type, "PENDIENTE").setIdReserva(id).setUuid("r-" + id).setNumeroReserva("N-" + id);
            rows.add(r);
            when(reservas.findByUuidForRevision(r.getUuid())).thenReturn(Optional.of(r));
        }
        List<Long> cursors = new ArrayList<>();
        Function<Long, Page<Reserva>> candidates = after -> {
            cursors.add(after);
            return new PageImpl<>(rows.stream().filter(r -> r.getIdReserva() > after)
                    .filter(r -> "PENDIENTE".equals(r.getEstadoReserva())).limit(100)
                    .collect(java.util.stream.Collectors.toList()));
        };
        ReservaRevisionPage<ReservaDto> page = (ReservaRevisionPage<ReservaDto>) reviewer.revisarLista(
                () -> new PageImpl<>(rows.subList(0, 5), PageRequest.of(0, 5), rows.size()),
                r -> r, com.gamq.deportes.dto.mapper.ReservaMapper::toDto, candidates);
        assertEquals(List.of(0L, 100L, 200L, 205L), cursors);
        assertEquals(5, page.getContent().size());
        assertEquals(205, page.getTotalElements());
        assertEquals(205, page.getRevision().getRevisadas());
        assertEquals(205, page.getRevision().getActualizadas());
        assertEquals(0, page.getRevision().getOmitidas());
        assertTrue(page.getRevision().isCompleta());
        assertEquals("RESERVAS_REVISABLES_AUTORIZADAS", page.getRevision().getAlcance());
        assertEquals("N-205", page.getRevision().getResultados().get(204).getReservaDto().getNumeroReserva());
        verify(ruat, times(205)).consultaPagoRevision(any());
        assertFalse(rows.stream().anyMatch(r -> "PENDIENTE".equals(r.getEstadoReserva())));
    }

    @Test void changedStatusesDoNotRestrictTheReviewToTheSelectedTabOrFirstPage() {
        paid();
        Reserva first = booking("periodo", "PENDIENTE").setIdReserva(1L);
        Reserva outsidePage = booking("periodo", "PAGADA").setIdReserva(7L).setUuid("outside");
        when(reservas.findByUuidForRevision(first.getUuid())).thenReturn(Optional.of(first));
        when(reservas.findByUuidForRevision(outsidePage.getUuid())).thenReturn(Optional.of(outsidePage));
        // Visible tab only shows completed; candidates include pending and paid from all pages.
        Supplier<Page<Reserva>> visible = () -> new PageImpl<>(List.of(first, outsidePage).stream()
                .filter(r -> "COMPLETADA".equals(r.getEstadoReserva())).collect(java.util.stream.Collectors.toList()));
        ReservaRevisionPage<ReservaDto> page = (ReservaRevisionPage<ReservaDto>) reviewer.revisarLista(
                visible, r -> r, com.gamq.deportes.dto.mapper.ReservaMapper::toDto,
                after -> after == 0 ? new PageImpl<>(List.of(first, outsidePage)) : Page.empty());
        assertEquals("PAGADA", first.getEstadoReserva());
        assertEquals("COMPLETADA", outsidePage.getEstadoReserva());
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getRevision().getActualizadas());
    }

    @Test void unavailableRuatStillReturnsPageAndChecksLocalProgressBeyondFirstBatch() {
        when(ruat.consultaPagoRevision(any())).thenThrow(new ResourceAccessException("offline"));
        List<Reserva> rows = new ArrayList<>();
        for (long id = 1; id <= 106; id++) {
            Reserva r = booking("periodo", id == 106 ? "PAGADA" : "PENDIENTE")
                    .setIdReserva(id).setUuid("r-" + id);
            rows.add(r);
            when(reservas.findByUuidForRevision(r.getUuid())).thenReturn(Optional.of(r));
        }
        ReservaRevisionPage<String> page = (ReservaRevisionPage<String>) reviewer.revisarLista(
                () -> new PageImpl<>(rows.subList(0, 5), PageRequest.of(0, 5), rows.size()),
                r -> r, Reserva::getEstadoReserva,
                after -> new PageImpl<>(rows.stream().filter(r -> r.getIdReserva() > after).limit(100)
                        .collect(java.util.stream.Collectors.toList())));
        assertEquals(5, page.getContent().size());
        assertEquals(1, page.getRevision().getFallidas());
        assertEquals(104, page.getRevision().getOmitidas());
        assertEquals(1, page.getRevision().getActualizadas());
        assertEquals("COMPLETADA", rows.get(105).getEstadoReserva());
        verify(ruat, times(1)).consultaPagoRevision(any());
    }

    @Test void readOnlyPaginationDoesNotInvokeAnyRuatReview() {
        Page<Reserva> rows = new PageImpl<>(List.of(booking("periodo", "PENDIENTE")));
        assertEquals(1, reviewer.leerPagina(() -> rows, Reserva::getNumeroReserva).getNumberOfElements());
        verifyNoInteractions(ruat);
        noWrites();
    }

    @ParameterizedTest @ValueSource(strings = {"ruat-be", "deportes-be"})
    void localErrorsCannotMasqueradeAsUnpaidRuatResponses(String origin) {
        when(ruat.consultaPagoRevision(any())).thenReturn(ResponseEntity.status(404)
                .header("X-Response-Origin", origin).body(unpaidBusiness(404).getBody()));
        assertEquals("ERROR", reviewer.revisarEstado(booking("puntual", "PENDIENTE")).getResultado());
        verify(ruat, never()).anularTasaRevision(any());
        noWrites();
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente"})
    void noAutomaticAnnulmentUnlessItsFlagIsExplicitlyEnabled(String type) {
        when(ruat.consultaPagoRevision(any())).thenReturn(unpaidBusiness(404));
        assertEquals("SIN_CAMBIOS", reviewer.revisarEstado(booking(type, "PENDIENTE")
                .setCancelacionAutomatica(null)).getResultado());
        verify(ruat, never()).anularTasaRevision(any());
        noWrites();
    }

    @Test void knownUnpaidQueriesDoNotInterruptTheRestOfTheFullReview() {
        when(ruat.consultaPagoRevision(any())).thenReturn(unpaidBusiness(404));
        List<Reserva> rows = new ArrayList<>();
        for (long id = 1; id <= 105; id++) {
            Reserva r = booking("periodo", "PENDIENTE").setIdReserva(id).setUuid("r-" + id);
            rows.add(r);
            when(reservas.findByUuidForRevision(r.getUuid())).thenReturn(Optional.of(r));
        }
        ReservaRevisionPage<String> page = (ReservaRevisionPage<String>) reviewer.revisarLista(
                () -> new PageImpl<>(rows.subList(0, 5), PageRequest.of(0, 5), rows.size()),
                r -> r, Reserva::getNumeroReserva,
                after -> new PageImpl<>(rows.stream().filter(r -> r.getIdReserva() > after).limit(100)
                        .collect(java.util.stream.Collectors.toList())));
        assertEquals(5, page.getContent().size());
        assertEquals(105, page.getRevision().getSinCambios());
        assertEquals(0, page.getRevision().getFallidas());
        assertEquals(0, page.getRevision().getOmitidas());
        assertTrue(page.getRevision().isCompleta());
        assertTrue(page.getRevision().getResultados().stream().allMatch(r -> NO_PAYMENT_MESSAGE.equals(r.getMotivo())));
        verify(ruat, times(105)).consultaPagoRevision(any());
        noWrites();
    }

    @ParameterizedTest @ValueSource(strings = {"", "Puntual", "Recurrente", "Periodo"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    void listEndpointsHonorReadOnlyPagingWithoutStartingReview(String kind) throws Exception {
        Object service = Class.forName("com.gamq.deportes.service.implement.Reserva" + kind + "ServiceImpl")
                .getDeclaredConstructor().newInstance();
        ReservaRuatRevisionService review = mock(ReservaRuatRevisionService.class);
        ReservationListScope scope = mock(ReservationListScope.class);
        when(scope.children()).thenReturn((root, query, cb) -> cb.conjunction());
        when(scope.periodos()).thenReturn((root, query, cb) -> cb.conjunction());
        when(scope.reservations()).thenReturn((root, query, cb) -> cb.conjunction());
        ReflectionTestUtils.setField(service, "listScope", scope);
        ReflectionTestUtils.setField(service, "ruatRevisionService", review);
        Page<?> expected = Page.empty();
        when(review.leerPagina(any(), any())).thenReturn((Page) expected);
        Object actual;
        if (kind.isEmpty()) {
            actual = ((ReservaServiceImpl) service).obtenerReservas(PageRequest.of(2, 5), 0, null, "periodo",
                    null, null, null, null, null, null, false);
        } else {
            Object filter = Class.forName("com.gamq.deportes.dto.Reserva" + kind + "Filter").getDeclaredConstructor().newInstance();
            ReflectionTestUtils.setField(filter, "revisarEstados", false);
            actual = ReflectionTestUtils.invokeMethod(service, "obtenerReservasConFiltros", filter, PageRequest.of(2, 5));
        }
        assertSame(expected, actual);
        verify(review).leerPagina(any(), any());
        verify(review, never()).revisarLista(any(), any(), any(), any());
    }

    @Test @SuppressWarnings({"unchecked", "rawtypes"})
    void actualMonthlyRootListUsesReviewerAndRetainsItsReport() {
        ReservaServiceImpl service = new ReservaServiceImpl();
        ReservaRuatRevisionService review = mock(ReservaRuatRevisionService.class);
        ReservationListScope scope = mock(ReservationListScope.class);
        when(scope.reservations()).thenReturn((root, query, cb) -> cb.conjunction());
        ReflectionTestUtils.setField(service, "listScope", scope);
        ReflectionTestUtils.setField(service, "ruatRevisionService", review);
        Page<ReservaDto> expected = new ReservaRevisionPage<>(Page.empty(), new ReservaRevisionPage.Resumen());
        when(review.revisarLista(any(), any(), any(), any())).thenReturn((Page) expected);
        assertSame(expected, service.obtenerReservas(PageRequest.of(0, 5), 0, null, "periodo",
                null, null, null, null, null, null, true));
        verify(scope).reservations();
        verify(review).revisarLista(any(), any(), any(), any());
    }

    @ParameterizedTest @ValueSource(strings = {"", "Puntual", "Recurrente", "Periodo"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    void actualListCandidateQueriesKeepAuthorizationButIgnoreVisualFiltersAndPage(String kind) throws Exception {
        Object service = Class.forName("com.gamq.deportes.service.implement.Reserva" + kind + "ServiceImpl")
                .getDeclaredConstructor().newInstance();
        Class<?> repoClass = Class.forName("com.gamq.deportes.repository.Reserva" + kind + "Repository");
        org.springframework.data.jpa.repository.JpaSpecificationExecutor repo =
                (org.springframework.data.jpa.repository.JpaSpecificationExecutor) mock(repoClass);
        ReservaRuatRevisionService review = mock(ReservaRuatRevisionService.class);
        ReservationListScope scope = mock(ReservationListScope.class);
        Specification authorized = mock(Specification.class, CALLS_REAL_METHODS);
        javax.persistence.criteria.Predicate ownership = mock(javax.persistence.criteria.Predicate.class);
        doReturn(ownership).when(authorized).toPredicate(any(), any(), any());
        when(scope.children()).thenReturn(authorized);
        when(scope.periodos()).thenReturn(authorized);
        when(scope.reservations()).thenReturn(authorized);
        ReflectionTestUtils.setField(service, "listScope", scope);
        ReflectionTestUtils.setField(service, "ruatRevisionService", review);
        ReflectionTestUtils.setField(service, "reserva" + kind + "Repository", repo);
        when(repo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        when(review.revisarLista(any(), any(), any(), any())).thenAnswer(invocation -> {
            Function<Long, Page<?>> candidates = invocation.getArgument(3);
            candidates.apply(100L);
            return Page.empty();
        });
        if (kind.isEmpty()) {
            ((ReservaServiceImpl) service).obtenerReservas(PageRequest.of(4, 5), 1, "view-only-search", "periodo",
                    "PENDIENTE", "APROBADA", null, null, 9, 2026, true);
        } else {
            Object filter = Class.forName("com.gamq.deportes.dto.Reserva" + kind + "Filter").getDeclaredConstructor().newInstance();
            ReflectionTestUtils.setField(filter, "estadoReserva", "PENDIENTE");
            ReflectionTestUtils.setField(filter, "searchTerm", "view-only-search");
            ReflectionTestUtils.invokeMethod(service, "obtenerReservasConFiltros", filter, PageRequest.of(4, 5));
        }
        ArgumentCaptor<Specification> spec = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> page = ArgumentCaptor.forClass(Pageable.class);
        verify(repo).findAll(spec.capture(), page.capture());
        assertEquals(0, page.getValue().getPageNumber());
        assertEquals(100, page.getValue().getPageSize());
        assertEquals(Sort.by(kind.isEmpty() ? "idReserva" : "reserva.idReserva"), page.getValue().getSort());
        javax.persistence.criteria.Root root = mock(javax.persistence.criteria.Root.class, RETURNS_DEEP_STUBS);
        javax.persistence.criteria.CriteriaQuery query = mock(javax.persistence.criteria.CriteriaQuery.class);
        javax.persistence.criteria.CriteriaBuilder cb = mock(javax.persistence.criteria.CriteriaBuilder.class, RETURNS_DEEP_STUBS);
        spec.getValue().toPredicate(root, query, cb);
        verify(authorized).toPredicate(root, query, cb); // No authorization bypass in the wider scan.
        verify(cb).and(eq(ownership), any());
        javax.persistence.criteria.Path reservation = kind.isEmpty() ? root : root.get("reserva");
        verify(reservation.get("estadoReserva")).in("SOLICITADA", "PENDIENTE", "PAGADA", "EN_CURSO");
        verify(cb).greaterThan(reservation.get("idReserva"), 100L);
        verify(cb, never()).equal(any(javax.persistence.criteria.Expression.class), eq("PENDIENTE"));
        verify(cb, never()).like(any(javax.persistence.criteria.Expression.class), anyString());
        if (kind.isEmpty()) verify(cb).equal(root.get("tipoReserva"), "periodo");
    }

    @ParameterizedTest @ValueSource(strings = {"Puntual", "Recurrente", "Periodo"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    void eachDetailListAlsoUsesReviewerAndRetainsItsReport(String kind) throws Exception {
        Object service = Class.forName("com.gamq.deportes.service.implement.Reserva" + kind + "ServiceImpl")
                .getDeclaredConstructor().newInstance();
        Object filter = Class.forName("com.gamq.deportes.dto.Reserva" + kind + "Filter").getDeclaredConstructor().newInstance();
        ReservaRuatRevisionService review = mock(ReservaRuatRevisionService.class);
        ReservationListScope scope = mock(ReservationListScope.class);
        when(scope.children()).thenReturn((root, query, cb) -> cb.conjunction());
        when(scope.periodos()).thenReturn((root, query, cb) -> cb.conjunction());
        ReflectionTestUtils.setField(service, "listScope", scope);
        ReflectionTestUtils.setField(service, "ruatRevisionService", review);
        Page<?> expected = new ReservaRevisionPage<>(Page.empty(), new ReservaRevisionPage.Resumen());
        when(review.revisarLista(any(), any(), any(), any())).thenReturn((Page) expected);
        Object actual = ReflectionTestUtils.invokeMethod(service, "obtenerReservasConFiltros", filter, PageRequest.of(0, 5));
        assertSame(expected, actual);
        verify(review).revisarLista(any(), any(), any(), any());
    }
}
