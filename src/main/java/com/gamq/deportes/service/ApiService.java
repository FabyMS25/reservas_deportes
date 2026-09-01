package com.gamq.deportes.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamq.deportes.model.AnularTasa.AnularTasaToken;
import com.gamq.deportes.model.Contribuyentes.ContribuyenteToken;
import com.gamq.deportes.model.Pagos.PagoToken;
import com.gamq.deportes.model.Registro.RegistroToken;
import com.gamq.deportes.model.Tasa.TasaToken;
import com.gamq.deportes.model.login.LoginRequest;
import com.gamq.deportes.model.login.LoginResponse;
public interface ApiService {

    LoginResponse login(LoginRequest loginRequest);
    LoginResponse loginWithConfiguredCredentials();
    JsonNode buscarContribuyente(ContribuyenteToken contribuyenteToken);
    JsonNode crearTasa(TasaToken tasaToken);
    JsonNode anularTasa(AnularTasaToken anularTasaToken);
    JsonNode registro(RegistroToken registroToken);
    JsonNode consultaPago(PagoToken pagoToken);
}
