package com.gamq.deportes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamq.deportes.dto.ConfiguracionSistemaDto;
import com.gamq.deportes.model.ConfiguracionSistema;
import com.gamq.deportes.repository.ConfiguracionSistemaRepository;
import com.gamq.deportes.security.ConfiguracionResolver;
import com.gamq.deportes.service.implement.ConfiguracionSistemaServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The login snapshot must carry every parameter the UI presents, but only for
 * the audience of the session and never the permission catalogue.
 */
class ConfiguracionSnapshotTest {

    private ConfiguracionSistemaServiceImpl service(List<ConfiguracionSistema> rows) {
        ConfiguracionSistemaRepository repository = mock(ConfiguracionSistemaRepository.class);
        when(repository.findAllActivas()).thenReturn(rows);
        return new ConfiguracionSistemaServiceImpl(repository, new ObjectMapper(),
                mock(SistemaAuditoriaService.class), mock(ConfiguracionResolver.class));
    }

    private ConfiguracionSistema row(String clave, String categoria, String aplicaA) {
        return new ConfiguracionSistema()
                .setClave(clave)
                .setCategoria(categoria)
                .setAplicaA(aplicaA)
                .setActivo(true)
                .setEstado(false)
                .setValorNumerico(1D);
    }

    private Set<String> keys(List<ConfiguracionSistemaDto> snapshot) {
        return snapshot.stream().map(ConfiguracionSistemaDto::getClave).collect(Collectors.toSet());
    }

    private List<ConfiguracionSistema> rows() {
        return List.of(
                row("HORAS_ANTICIPACION_RESERVAS_PUNTUALES", "REGLAS_RESERVA", "contribuyente"),
                row("HORAS_VALIDACION_PAGO", "POLITICAS_PAGO", "global"),
                row("LIMITE_REPRESENTANTE", "LIMITES", "representante"),
                row("HABILITAR_RESERVAS_ORGANIZACIONES", "PERMISSIONS", "contribuyente"),
                row("HABILITAR_RESERVAS_RECURRENTES", "PERMISSIONS", "global"),
                row("RUAT_ESTADO", "RUAT", "global"));
    }

    @Test
    void contributorSnapshotIncludesContributorRulesAndExcludesPermissions() {
        Set<String> snapshot = keys(service(rows()).obtenerConfiguracionesParaCliente(false));

        assertTrue(snapshot.contains("HORAS_ANTICIPACION_RESERVAS_PUNTUALES"));
        assertTrue(snapshot.contains("HORAS_VALIDACION_PAGO"));
        assertFalse(snapshot.contains("LIMITE_REPRESENTANTE"));
        assertFalse(snapshot.contains("HABILITAR_RESERVAS_ORGANIZACIONES"));
        assertFalse(snapshot.contains("HABILITAR_RESERVAS_RECURRENTES"));
        assertFalse(snapshot.contains("RUAT_ESTADO"));
    }

    @Test
    void representativeSnapshotAlsoIncludesRepresentativeRows() {
        Set<String> snapshot = keys(service(rows()).obtenerConfiguracionesParaCliente(true));

        assertTrue(snapshot.contains("LIMITE_REPRESENTANTE"));
        assertTrue(snapshot.contains("HORAS_ANTICIPACION_RESERVAS_PUNTUALES"));
        assertFalse(snapshot.contains("HABILITAR_RESERVAS_ORGANIZACIONES"));
    }

    @Test
    void staffSnapshotKeepsOnlyGlobalRows() {
        Set<String> snapshot = keys(service(rows()).obtenerConfiguracionesParaCliente());

        assertEquals(Set.of("HORAS_VALIDACION_PAGO"), snapshot);
    }
}
