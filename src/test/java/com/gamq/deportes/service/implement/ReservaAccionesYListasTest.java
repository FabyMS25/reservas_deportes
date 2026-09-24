package com.gamq.deportes.service.implement;

import com.gamq.deportes.controller.*;
import com.gamq.deportes.dto.*;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.*;
import org.hibernate.annotations.Formula;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaAccionesYListasTest {
    @Test void actionRoutesUseQueryUuidAndGenericChildPutIsNotExposed() throws Exception {
        for (String[] route : new String[][]{{"completarReservaEscuela", "/completar-escuela"},
                {"completarUso", "/completar"}, {"marcarNoShow", "/update-not-show"}}) {
            var method = "marcarNoShow".equals(route[0])
                    ? ReservaController.class.getMethod(route[0], String.class, String.class)
                    : ReservaController.class.getMethod(route[0], String.class);
            assertEquals(route[1], method.getAnnotation(PutMapping.class).value()[0]);
            assertEquals("uuid", method.getParameters()[0].getAnnotation(RequestParam.class).value());
            assertEquals("@permissions.reservation('ACTUALIZAR',#p0)", method.getAnnotation(
                    org.springframework.security.access.prepost.PreAuthorize.class).value());
        }
        for (Class<?> controller : List.of(ReservaRecurrenteController.class, ReservaPuntualController.class)) {
            for (var method : controller.getDeclaredMethods()) {
                PutMapping mapping = method.getAnnotation(PutMapping.class);
                if (mapping != null) assertTrue(mapping.value().length > 0, "Generic child PUT must not remain exposed");
            }
        }
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente"})
    void useActionsOnlyChangeRootState(String type) {
        for (String state : List.of("PAGADA", "EN_CURSO")) for (String action : List.of("NO_SHOW", "COMPLETADA")) {
            Reserva root = new Reserva().setUuid("r").setTipoReserva(type).setEstadoReserva(state)
                    .setEstadoValidacion("APROBADA").setNumeroTasa("42").setMontoTotal(new BigDecimal("99.50"));
            ReservaRepository repository = mock(ReservaRepository.class);
            when(repository.findByUuidForRevision("r")).thenReturn(Optional.of(root));
            when(repository.save(root)).thenReturn(root);
            ReservaServiceImpl service = new ReservaServiceImpl();
            ReflectionTestUtils.setField(service, "reservaRepository", repository);
            ReflectionTestUtils.setField(service, "reservaAuditoriaRepository", mock(ReservaAuditoriaRepository.class));
            assertEquals(action, service.actualizarEstadoUso("r", action, "Observación del usuario").getEstadoReserva());
            assertEquals("42", root.getNumeroTasa());
            assertEquals(new BigDecimal("99.50"), root.getMontoTotal());
            verify(repository).save(root);
            service.actualizarEstadoUso("r", action, null);
            verify(repository, times(1)).save(root);
        }
    }

    @ParameterizedTest @ValueSource(strings = {"SOLICITADA", "PENDIENTE", "CANCELADA", "EXPIRADA", "COMPLETADA"})
    void noShowRejectsInvalidStates(String state) {
        Reserva root = new Reserva().setUuid("r").setTipoReserva("puntual").setEstadoReserva(state).setEstadoValidacion("APROBADA");
        ReservaRepository repository = mock(ReservaRepository.class);
        when(repository.findByUuidForRevision("r")).thenReturn(Optional.of(root));
        ReservaServiceImpl service = new ReservaServiceImpl();
        ReflectionTestUtils.setField(service, "reservaRepository", repository);
        assertThrows(IllegalStateException.class, () -> service.actualizarEstadoUso("r", "NO_SHOW", null));
        verify(repository, never()).save(any());
    }

    @ParameterizedTest @ValueSource(strings = {"puntual", "recurrente", "periodo"})
    void registeredRateBlocksGenericDataEdits(String type) {
        Reserva root = new Reserva().setUuid("r").setTipoReserva(type).setEstadoReserva("PENDIENTE")
                .setNumeroTasa("42").setMontoTotal(BigDecimal.TEN);
        ReservaRepository repository = mock(ReservaRepository.class);
        when(repository.findByUuid("r")).thenReturn(Optional.of(root));
        ReservaServiceImpl service = new ReservaServiceImpl();
        ReflectionTestUtils.setField(service, "reservaRepository", repository);
        assertThrows(IllegalStateException.class, () -> service.actualizarReserva(new ReservaDto().setUuid("r").setMontoTotal(BigDecimal.ONE)));
        assertEquals(BigDecimal.TEN, root.getMontoTotal());
        verify(repository, never()).save(any());
    }

    @Test void monthlyDetailsWithRateCannotBeEdited() {
        ReservaPeriodo period = new ReservaPeriodo().setUuid("p").setMes(1).setAño(2026)
                .setReserva(new Reserva().setNumeroTasa("42"));
        ReservaPeriodoRepository repository = mock(ReservaPeriodoRepository.class);
        when(repository.findByUuid("p")).thenReturn(Optional.of(period));
        ReservaPeriodoServiceImpl service = new ReservaPeriodoServiceImpl();
        ReflectionTestUtils.setField(service, "reservaPeriodoRepository", repository);
        assertThrows(IllegalStateException.class, () -> service.actualizarReservaPeriodo(new ReservaPeriodoDto().setUuid("p").setMes(2)));
        assertEquals(1, period.getMes());
        verify(repository, never()).save(any());
    }

    @Test void tariffDetailWithRateCannotBeEdited() {
        ReservaTarifaDetalle detail = new ReservaTarifaDetalle().setUuid("d")
                .setReserva(new Reserva().setNumeroTasa("42"));
        ReservaTarifaDetalleRepository repository = mock(ReservaTarifaDetalleRepository.class);
        when(repository.findByUuid("d")).thenReturn(Optional.of(detail));
        ReservaTarifaDetalleServiceImpl service = new ReservaTarifaDetalleServiceImpl();
        ReflectionTestUtils.setField(service, "reservaTarifaDetalleRepository", repository);
        assertThrows(IllegalStateException.class, () -> service.actualizarReservaTarifaDetalle(new ReservaTarifaDetalleDto().setUuid("d")));
        verify(repository, never()).save(any());
    }

    @Test void reschedulingPreservesRateAmountsDurationAndRootState() {
        Reserva root = new Reserva().setUuid("r").setEstadoReserva("PENDIENTE").setNumeroTasa("42")
                .setMontoTotal(BigDecimal.TEN).setCampoDeportivo(new CampoDeportivo().setUuid("field")
                        .setSuperficie(new Superficie()).setComplejo(new ComplejoDeportivo()));
        ReservaPuntual row = new ReservaPuntual().setUuid("p").setReserva(root).setFechaUso(LocalDate.now().plusDays(10))
                .setHoraInicio(LocalTime.of(10, 0)).setHoraFin(LocalTime.of(11, 0)).setDuracionHoras(1)
                .setPrecioBaseAplicado(BigDecimal.TEN).setCantidadPersonas(1);
        ReservaPuntualRepository childRepo = mock(ReservaPuntualRepository.class);
        ReservaRepository roots = mock(ReservaRepository.class);
        when(childRepo.findByUuid("p")).thenReturn(Optional.of(row));
        when(childRepo.save(row)).thenReturn(row);
        when(roots.findByUuidForRevision("r")).thenReturn(Optional.of(root));
        ReservaPuntualServiceImpl service = spy(new ReservaPuntualServiceImpl());
        ReflectionTestUtils.setField(service, "reservaPuntualRepository", childRepo);
        ReflectionTestUtils.setField(service, "reservaRepository", roots);
        ReflectionTestUtils.setField(service, "reservaAuditoriaRepository", mock(ReservaAuditoriaRepository.class));
        doReturn(false).when(service).existeReservaConflictiva(any(), any(), any(), any(), any());
        ReservaPuntualDto change = new ReservaPuntualDto().setUuid("p").setFechaUso(LocalDate.now().plusDays(12))
                .setHoraInicio(LocalTime.of(12, 0)).setHoraFin(LocalTime.of(13, 0))
                .setPrecioBaseAplicado(BigDecimal.ONE).setCantidadPersonas(999)
                .setReservaDto(new ReservaDto().setUuid("other").setEstadoReserva("COMPLETADA"));
        service.actualizarReservaPuntual(change);
        assertSame(root, row.getReserva());
        assertEquals("PENDIENTE", root.getEstadoReserva());
        assertEquals("42", root.getNumeroTasa());
        assertEquals(BigDecimal.TEN, row.getPrecioBaseAplicado());
        assertEquals(1, row.getCantidadPersonas());
        assertEquals(LocalTime.of(12, 0), row.getHoraInicio());
        clearInvocations(childRepo);
        root.setEstadoReserva("PAGADA");
        assertThrows(IllegalStateException.class, () -> service.actualizarReservaPuntual(change));
        root.setEstadoReserva("PENDIENTE");
        change.setHoraFin(LocalTime.of(14, 0));
        assertThrows(IllegalArgumentException.class, () -> service.actualizarReservaPuntual(change));
        change.setHoraFin(LocalTime.of(13, 0));
        doReturn(true).when(service).existeReservaConflictiva(any(), any(), any(), any(), any());
        assertThrows(IllegalStateException.class, () -> service.actualizarReservaPuntual(change));
        verify(childRepo, never()).save(any());
    }

    @Test void sortingUsesMappedScalarFieldsAndStableTieBreaker() throws Exception {
        String[] common = {"numeroReserva", "contribuyenteDto.nombreCompleto", "campoDeportivoDto.complejoDto.nombre",
                "campoDeportivoDto.nombre", "tarifaDto.clasificadorRuatDto.nombre", "tarifaDto.tipoCobro", "organizacionDto.nombre",
                "organizacionDto.tipoOrganizacion", "organizacionDto.disciplina", "organizacionDto.entrenador", "montoTotal", "numeroTasa",
                "fechaLimitePago", "cancelacionAutomatica", "esReservaGratuita", "motivoGratuidad", "estadoValidacion", "estadoReserva",
                "pagosDtoList[0].estadoPago", "fechaCreacion"};
        for (String kind : List.of("puntual", "recurrente", "periodo")) for (String column : common) {
            var sort = ReservaServiceImpl.ordenListado(("periodo".equals(kind) ? "" : "reservaDto.") + column, "desc", kind);
            assertEquals(2, sort.stream().count());
            assertTrue(sort.iterator().next().isDescending());
            for (var order : sort) {
                Class<?> entity = "puntual".equals(kind) ? ReservaPuntual.class : "recurrente".equals(kind) ? ReservaRecurrente.class : Reserva.class;
                for (String part : order.getProperty().split("\\.")) {
                    var field = entity.getDeclaredField(part);
                    assertNull(field.getAnnotation(javax.persistence.Transient.class));
                    assertFalse(Collection.class.isAssignableFrom(field.getType()), "Sorting must not join collections");
                    entity = field.getType();
                }
            }
        }
        assertEquals("fechaUso", ReservaServiceImpl.ordenListado("arbitrary.private.path", "asc", "puntual").iterator().next().getProperty());
    }

    @Test void paymentFormulaCorrelatesToOuterReservationAndUsesDisplayOrder() throws Exception {
        String formula = Reserva.class.getDeclaredField("estadoPagoOrden").getAnnotation(Formula.class).value();
        var dialect = new org.hibernate.dialect.PostgreSQL95Dialect();
        String rendered = org.hibernate.sql.Template.renderWhereStringTemplate(formula, "r", dialect,
                new org.hibernate.dialect.function.SQLFunctionRegistry(dialect, Map.of()));
        assertTrue(rendered.contains("p.id_reserva = r.id_reserva"), rendered);
        assertTrue(rendered.contains("p.estado = false"), rendered);
        assertEquals("idPago ASC", Reserva.class.getDeclaredField("pagosList").getAnnotation(javax.persistence.OrderBy.class).value());
    }

    @Test void hibernateCanRenderFullNameFormula() throws Exception {
        String formula = Contribuyente.class.getDeclaredField("nombreCompletoOrden").getAnnotation(Formula.class).value();
        var dialect = new org.hibernate.dialect.PostgreSQL95Dialect();
        String rendered = org.hibernate.sql.Template.renderWhereStringTemplate(formula, "c", dialect,
                new org.hibernate.dialect.function.SQLFunctionRegistry(dialect, Map.of()));
        assertTrue(rendered.contains("c.nombre"), rendered);
        assertTrue(rendered.contains("c.primer_apellido"), rendered);
        assertTrue(rendered.contains("c.segundo_apellido"), rendered);
    }
}
