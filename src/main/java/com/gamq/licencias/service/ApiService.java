package com.gamq.licencias.service;

import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.model.AnularTasa.AnularTasaResponse;
import com.gamq.licencias.model.AnularTasa.AnularTasaToken;
import com.gamq.licencias.model.Contribuyente.ContribuyenteRequest;
import com.gamq.licencias.model.Contribuyente.ContribuyenteResponse;
import com.gamq.licencias.model.Contribuyente.ContribuyenteToken;
import com.gamq.licencias.model.Pagos.PagoResponse;
import com.gamq.licencias.model.Pagos.PagoToken;
import com.gamq.licencias.model.Registro.RegistroRequest;
import com.gamq.licencias.model.Registro.RegistroResponse;
import com.gamq.licencias.model.Registro.RegistroToken;
import com.gamq.licencias.model.Tasa.TasaResponse;
import com.gamq.licencias.model.Tasa.TasaToken;
import com.gamq.licencias.model.login.LoginRequest;
import com.gamq.licencias.model.login.LoginResponse;
import org.springframework.stereotype.Service;

@Service
public interface ApiService {

    LoginResponse login(LoginRequest loginRequest);
    ContribuyenteResponse buscarContribuyente(ContribuyenteToken contribuyenteToken);
    TasaResponse crearTasa(TasaToken tasaToken);
    AnularTasaResponse anularTasa(AnularTasaToken anularTasaToken);
    RegistroResponse registro(RegistroToken registroToken);
    PagoResponse consultaPago (PagoToken pagoToken);
}
