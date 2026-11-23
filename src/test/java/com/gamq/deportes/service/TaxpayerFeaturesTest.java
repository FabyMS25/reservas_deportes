package com.gamq.deportes.service;

import com.gamq.deportes.security.ConfiguracionResolver;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Configuration becomes one set of menu features at login; no capability borrows another's flag. */
class TaxpayerFeaturesTest {

    private AuthenticationService service(ConfiguracionResolver resolver) {
        return new AuthenticationService(null, null, null, null, null, null,
                "", "SIS-013", "DEPORTESAPP", "", null, null, null, null, null, resolver);
    }

    @Test
    void organizationDirectDoesNotEnableRecurrentOrEventDirect() {
        ConfiguracionResolver resolver = mock(ConfiguracionResolver.class);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_RESERVAS_ORGANIZACIONES", true)).thenReturn(true);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_RESERVAS_RECURRENTES", true)).thenReturn(false);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_SOLICITUD_RESERVAS_RECURRENTES", true)).thenReturn(true);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_RESERVAS_EVENTOS", true)).thenReturn(false);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_SOLICITUD_RESERVAS_EVENTOS", true)).thenReturn(true);

        Set<String> features = service(resolver).taxpayerFeatures(true);

        assertTrue(features.contains("ORGANIZATION_DIRECT"));
        assertFalse(features.contains("ORGANIZATION_REQUEST"));
        assertFalse(features.contains("RECURRENT_CREATE"));
        assertTrue(features.contains("RECURRENT_REQUEST"));
        assertFalse(features.contains("EVENT_CREATE"));
        assertTrue(features.contains("EVENT_REQUEST"));
    }

    @Test
    void directCapabilityWinsOverItsRequestFlag() {
        ConfiguracionResolver resolver = mock(ConfiguracionResolver.class);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_RESERVAS_ORGANIZACIONES", true)).thenReturn(false);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_SOLICITUD_RESERVAS_ORGANIZACIONES", true)).thenReturn(true);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_RESERVAS_RECURRENTES", true)).thenReturn(true);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_SOLICITUD_RESERVAS_RECURRENTES", true)).thenReturn(true);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_RESERVAS_EVENTOS", true)).thenReturn(true);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_SOLICITUD_RESERVAS_EVENTOS", true)).thenReturn(true);

        Set<String> features = service(resolver).taxpayerFeatures(true);

        assertFalse(features.contains("ORGANIZATION_DIRECT"));
        assertTrue(features.contains("ORGANIZATION_REQUEST"));
        assertTrue(features.contains("RECURRENT_CREATE"));
        assertFalse(features.contains("RECURRENT_REQUEST"));
        assertTrue(features.contains("EVENT_CREATE"));
        assertFalse(features.contains("EVENT_REQUEST"));
    }

    @Test
    void nonRepresentativeNeverGetsOrganizationFeatures() {
        ConfiguracionResolver resolver = mock(ConfiguracionResolver.class);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_RESERVAS_ORGANIZACIONES", false)).thenReturn(true);
        when(resolver.permisoHabilitadoParaAudiencia("HABILITAR_SOLICITUD_RESERVAS_ORGANIZACIONES", false)).thenReturn(true);

        Set<String> features = service(resolver).taxpayerFeatures(false);

        assertFalse(features.contains("ORGANIZATION_DIRECT"));
        assertFalse(features.contains("ORGANIZATION_REQUEST"));
    }
}
