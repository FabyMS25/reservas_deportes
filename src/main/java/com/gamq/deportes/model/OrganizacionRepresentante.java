package com.gamq.deportes.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizaciones_representantes")
@Getter
@Setter
@NoArgsConstructor
public class OrganizacionRepresentante {
    @EmbeddedId
    private OrganizacionRepresentanteId id;

    @MapsId("idOrganizacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organizacion")
    private Organizacion organizacion;

    @MapsId("idContribuyente")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contribuyente")
    private Contribuyente contribuyente;
}
