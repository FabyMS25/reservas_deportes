package com.gamq.deportes.repository;

import com.gamq.deportes.model.OrganizacionRepresentante;
import com.gamq.deportes.model.OrganizacionRepresentanteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizacionRepresentanteRepository
        extends JpaRepository<OrganizacionRepresentante, OrganizacionRepresentanteId> {
    List<OrganizacionRepresentante> findByOrganizacionIdOrganizacion(Long id);

    List<OrganizacionRepresentante> findByContribuyenteIdContribuyente(Long id);
}
