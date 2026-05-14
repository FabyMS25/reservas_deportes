package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuatApiResponseDto {
    private String mensaje;
    private Boolean continuarFlujo;
    private Object datos;
    private Object detalle;
}
