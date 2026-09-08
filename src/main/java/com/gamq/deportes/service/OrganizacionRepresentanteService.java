package com.gamq.deportes.service;

import com.gamq.deportes.dto.OrganizacionRepresentanteDto;
import com.gamq.deportes.model.Contribuyente;
import com.gamq.deportes.model.Organizacion;
import com.gamq.deportes.model.OrganizacionRepresentante;
import com.gamq.deportes.model.OrganizacionRepresentanteId;
import com.gamq.deportes.repository.ContribuyenteRepository;
import com.gamq.deportes.repository.OrganizacionRepresentanteRepository;
import com.gamq.deportes.repository.OrganizacionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class OrganizacionRepresentanteService {
    private final OrganizacionRepresentanteRepository representantes;
    private final OrganizacionRepository organizaciones;
    private final ContribuyenteRepository contribuyentes;

    public OrganizacionRepresentanteService(OrganizacionRepresentanteRepository representantes,
            OrganizacionRepository organizaciones,
            ContribuyenteRepository contribuyentes) {
        this.representantes = representantes;
        this.organizaciones = organizaciones;
        this.contribuyentes = contribuyentes;
    }

    public void guardarRepresentantes(Organizacion organizacion, List<OrganizacionRepresentanteDto> values) {
        if (values == null) return;
        organizaciones.findByUuidForUpdate(organizacion.getUuid());

        Set<Long> selected = new HashSet<>();
        if (organizacion.getRepresentante() != null) {
            selected.add(organizacion.getRepresentante().getIdContribuyente());
            guardar(organizacion, organizacion.getRepresentante());
        }

        for (OrganizacionRepresentanteDto value : values) {
            if (value == null || value.getContribuyenteDto() == null
                    || value.getContribuyenteDto().getUuid() == null) {
                throw new IllegalArgumentException("Seleccione un contribuyente registrado");
            }
            Contribuyente contribuyente = contribuyentes.findByUuid(value.getContribuyenteDto().getUuid())
                    .orElseThrow(() -> new IllegalArgumentException("Contribuyente no encontrado"));
            if (!selected.add(contribuyente.getIdContribuyente())) {
                throw new IllegalArgumentException("Representante duplicado");
            }
            guardar(organizacion, contribuyente);
        }

        for (OrganizacionRepresentante link : representantes.findByOrganizacionIdOrganizacion(
                organizacion.getIdOrganizacion())) {
            if (!selected.contains(link.getId().getIdContribuyente())) {
                eliminar(link);
            }
        }
    }

    public void guardarOrganizacionesRepresentadas(Contribuyente contribuyente,
            List<OrganizacionRepresentanteDto> values) {
        if (values == null) return;

        Set<Long> selected = new HashSet<>();
        for (OrganizacionRepresentanteDto value : values) {
            if (value == null || value.getOrganizacionDto() == null
                    || value.getOrganizacionDto().getUuid() == null) {
                throw new IllegalArgumentException("Seleccione una organización existente");
            }
            Organizacion organizacion = organizaciones.findByUuidForUpdate(value.getOrganizacionDto().getUuid())
                    .orElseThrow(() -> new IllegalArgumentException("Organización no encontrada"));
            if (!selected.add(organizacion.getIdOrganizacion())) {
                throw new IllegalArgumentException("Organización duplicada");
            }
            guardar(organizacion, contribuyente);
        }

        for (OrganizacionRepresentante link : representantes.findByContribuyenteIdContribuyente(
                contribuyente.getIdContribuyente())) {
            boolean esPrincipal = link.getOrganizacion().getRepresentante() != null
                    && contribuyente.getUuid().equals(link.getOrganizacion().getRepresentante().getUuid());
            if (!esPrincipal && !selected.contains(link.getId().getIdOrganizacion())) {
                eliminar(link);
            }
        }
    }

    public void eliminarPorOrganizacion(Organizacion organizacion) {
        representantes.findByOrganizacionIdOrganizacion(organizacion.getIdOrganizacion())
                .forEach(representantes::delete);
    }

    private void guardar(Organizacion organizacion, Contribuyente contribuyente) {
        if (!contribuyente.estaActivo()) {
            throw new IllegalArgumentException("El contribuyente debe estar activo");
        }
        OrganizacionRepresentanteId id = new OrganizacionRepresentanteId(
                organizacion.getIdOrganizacion(), contribuyente.getIdContribuyente());
        if (representantes.existsById(id)) return;

        exigirPersonalMunicipal();
        OrganizacionRepresentante link = new OrganizacionRepresentante();
        link.setId(id);
        link.setOrganizacion(organizacion);
        link.setContribuyente(contribuyente);
        organizacion.getRepresentantes().add(link);
        contribuyente.getOrganizacionRepresentanteLinks().add(link);
        representantes.save(link);
    }

    private void eliminar(OrganizacionRepresentante link) {
        organizaciones.findByUuidForUpdate(link.getOrganizacion().getUuid());
        exigirPersonalMunicipal();
        representantes.delete(link);
    }

    public static void exigirPersonalMunicipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(a ->
                "DEPORTES_STAFF".equals(a.getAuthority()) || "DEPORTES_ADMIN".equals(a.getAuthority()))) {
            throw new AccessDeniedException("Solo el personal municipal puede asignar representantes");
        }
    }
}
