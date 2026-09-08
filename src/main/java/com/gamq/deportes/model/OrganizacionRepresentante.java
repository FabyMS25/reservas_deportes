package com.gamq.deportes.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

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
