package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** API contract for a Borderoux pre-liquidation, independent of booking DTOs. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservaBorderouxDto {
    private String uuid;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String numeroReserva;
    private String numeroTasa;
    private BigDecimal montoTotal;
    private String estadoReserva;
    private String estadoValidacion;
    private String clasificadorRuatId;
    private String creadoPor;
    private OrganizacionDto organizacionDto;
    private ContribuyenteDto contribuyenteDto;
    private List<ReservaBorderouxActividadDto> borderouxActividadesDtoList = new ArrayList<>();
}
