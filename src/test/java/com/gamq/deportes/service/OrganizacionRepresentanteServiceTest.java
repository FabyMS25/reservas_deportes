package com.gamq.deportes.service;

import com.gamq.deportes.dto.ContribuyenteDto;
import com.gamq.deportes.dto.OrganizacionRepresentanteDto;
import com.gamq.deportes.dto.mapper.ContribuyenteMapper;
import com.gamq.deportes.model.Contribuyente;
import com.gamq.deportes.model.Organizacion;
import com.gamq.deportes.model.OrganizacionRepresentante;
import com.gamq.deportes.model.OrganizacionRepresentanteId;
import com.gamq.deportes.repository.ContribuyenteRepository;
import com.gamq.deportes.repository.OrganizacionRepresentanteRepository;
import com.gamq.deportes.repository.OrganizacionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrganizacionRepresentanteServiceTest {
    OrganizacionRepresentanteRepository links = mock(OrganizacionRepresentanteRepository.class);
    OrganizacionRepository orgs = mock(OrganizacionRepository.class);
    ContribuyenteRepository people = mock(ContribuyenteRepository.class);
    OrganizacionRepresentanteService service = new OrganizacionRepresentanteService(links, orgs, people);
    Organizacion org = new Organizacion().setIdOrganizacion(1L).setUuid("school");
    Contribuyente person = new Contribuyente().setIdContribuyente(2L).setUuid("coach").setEstadoUsuario("ACTIVO");

    @BeforeEach
    void setup() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "staff", "", Collections.singletonList(new SimpleGrantedAuthority("DEPORTES_STAFF"))));
        when(people.findByUuid("coach")).thenReturn(Optional.of(person));
        when(orgs.findByUuidForUpdate("school")).thenReturn(Optional.of(org));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    OrganizacionRepresentanteDto dto() {
        return new OrganizacionRepresentanteDto()
                .setContribuyenteDto(new ContribuyenteDto().setUuid("coach"));
    }

    OrganizacionRepresentante existing() {
        OrganizacionRepresentante link = new OrganizacionRepresentante();
        link.setId(new OrganizacionRepresentanteId(1L, 2L));
        link.setOrganizacion(org);
        link.setContribuyente(person);
        return link;
    }

    @Test
    void representativesAreSavedFromSelectedListOnly() {
        service.guardarRepresentantes(org, Collections.singletonList(dto()));

        assertTrue(org.tieneRepresentante("coach"));
        assertEquals(1, person.getOrganizacionRepresentanteLinks().size());
        verify(links, times(1)).save(any());
    }

    @Test
    void missingRelationshipPayloadPreservesLinks() {
        service.guardarRepresentantes(org, null);

        verifyNoInteractions(links, orgs, people);
    }

    @Test
    void removalCannotLeaveOrganizationWithoutRepresentatives() {
        OrganizacionRepresentante link = existing();
        when(links.findByOrganizacionIdOrganizacion(1L)).thenReturn(Collections.singletonList(link));

        assertThrows(IllegalArgumentException.class,
                () -> service.guardarRepresentantes(org, Collections.emptyList()));

        verify(links, never()).delete(link);
        verify(orgs, never()).save(any());
        verify(orgs, never()).delete(any());
    }

    @Test
    void taxpayerCannotGrantThemselvesMembership() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "taxpayer", "", Collections.singletonList(new SimpleGrantedAuthority("DEPORTES_TAXPAYER"))));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.guardarRepresentantes(org, Collections.singletonList(dto())));
        verify(links, never()).save(any());
    }

    @Test
    void mapperBuildsRepresentedOrganizationsFromRelationshipTable() {
        OrganizacionRepresentante link = existing();
        org.getRepresentantes().add(link);
        person.getOrganizacionRepresentanteLinks().add(link);

        ContribuyenteDto result = ContribuyenteMapper.toDto(person, true);

        assertEquals(1, result.getOrganizacionesRepresentadas().size());
        assertEquals(1, result.getOrganizacionesVinculadas().size());
        assertEquals("coach", result.getOrganizacionesRepresentadas().get(0)
                .getRepresentantesDto().get(0).getContribuyenteDto().getUuid());
    }
}

