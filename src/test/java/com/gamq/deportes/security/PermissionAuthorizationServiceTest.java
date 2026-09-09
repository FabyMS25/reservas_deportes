package com.gamq.deportes.security;

import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionAuthorizationServiceTest {
    private final ReservaRepository reservations = mock(ReservaRepository.class);
    private final OrganizacionRepository organizations = mock(OrganizacionRepository.class);
    private final PermissionAuthorizationService service = new PermissionAuthorizationService(reservations,
            mock(ReservaPuntualRepository.class), mock(ReservaRecurrenteRepository.class),
            mock(ReservaPeriodoRepository.class), organizations, mock(EncargadoComplejoRepository.class),
            mock(CampoDeportivoRepository.class), mock(NotificacionRepository.class),
            mock(ReservaTarifaDetalleRepository.class), mock(PagoRepository.class));

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    private void login(String type, boolean admin, String... keys) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new DeportesPrincipal("user", type, admin, Set.of(keys)), null, Collections.emptyList()));
    }
    @Test void deniesAnonymousUnknownPermissionAndAdminWithoutTheAction() {
        assertFalse(service.has("Categorias", "CREAR"));
        login("STAFF", true, "cat:v");
        assertTrue(service.has("Categorias", "VER"));
        assertFalse(service.has("Categorias", "CREAR"));
        assertFalse(service.has("Unknown", "VER"));
    }
    @Test void representativeCannotUpdateAnotherOrganizationsReservation() {
        login("TAXPAYER", false, "sch:u");
        Organizacion org = mock(Organizacion.class);
        when(org.getUuid()).thenReturn("org");
        when(org.tieneRepresentante("user")).thenReturn(false);
        when(organizations.findByUuid("org")).thenReturn(Optional.of(org));
        Reserva r = new Reserva().setUuid("reservation").setTipoReserva("periodo").setOrganizacion(org);
        when(reservations.findByUuid("reservation")).thenReturn(Optional.of(r));
        assertFalse(service.reservation("ACTUALIZAR", "reservation"));
        when(org.tieneRepresentante("user")).thenReturn(true);
        assertTrue(service.reservation("ACTUALIZAR", "reservation"));
        // Removing the current relationship takes effect despite the signed snapshot.
        when(org.tieneRepresentante("user")).thenReturn(false);
        assertFalse(service.reservation("ACTUALIZAR", "reservation"));
    }
}
