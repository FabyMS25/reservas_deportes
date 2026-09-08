package com.gamq.deportes.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizacionRepresentanteId implements Serializable {
    @Column(name = "id_organizacion")
    private Long idOrganizacion;

    @Column(name = "id_contribuyente")
    private Long idContribuyente;
}
