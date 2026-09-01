package com.gamq.deportes.model.Tasa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DatosConcepto {
    // Used by the frontend to enumerate concepts. RUAT's DatosConcepto contract
    // does not accept this property, so it must never be serialized outbound.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String numero;
    private String codigoClasificador;
    private String tipoArancel;
    private String monto;
}
