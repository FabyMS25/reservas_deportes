package com.gamq.deportes.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class OrganizacionRepresentanteDto {
    private OrganizacionDto organizacionDto;
    private ContribuyenteDto contribuyenteDto;
}
