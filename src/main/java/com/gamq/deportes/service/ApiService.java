package com.gamq.deportes.service;

import com.gamq.deportes.model.AnularTasa.AnularTasaResponse;
import com.gamq.deportes.model.AnularTasa.AnularTasaToken;
import com.gamq.deportes.model.Contribuyente.ContribuyenteResponse;
import com.gamq.deportes.model.Contribuyente.ContribuyenteToken;
import com.gamq.deportes.model.Pagos.PagoResponse;
import com.gamq.deportes.model.Pagos.PagoToken;
import com.gamq.deportes.model.Registro.RegistroResponse;
import com.gamq.deportes.model.Registro.RegistroToken;
import com.gamq.deportes.model.Tasa.TasaResponse;
import com.gamq.deportes.model.Tasa.TasaToken;
import com.gamq.deportes.model.login.LoginRequest;
import com.gamq.deportes.model.login.LoginResponse;
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
