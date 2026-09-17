package com.gamq.deportes.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import com.gamq.deportes.model.AnularTasa.AnularTasaToken;
import com.gamq.deportes.model.Contribuyentes.ContribuyenteToken;
import com.gamq.deportes.model.Pagos.PagoToken;
import com.gamq.deportes.model.Registro.RegistroToken;
import com.gamq.deportes.model.Tasa.TasaToken;
public interface ApiService {

    JsonNode checkRuatAvailability();
    ResponseEntity<byte[]> buscarContribuyente(ContribuyenteToken contribuyenteToken);
    ResponseEntity<byte[]> crearTasa(TasaToken tasaToken);
    ResponseEntity<byte[]> anularTasa(AnularTasaToken anularTasaToken);
    ResponseEntity<byte[]> registro(RegistroToken registroToken);
    ResponseEntity<byte[]> consultaPago(PagoToken pagoToken);
}
