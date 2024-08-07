package com.gamq.licencias.service.implement;

import com.gamq.licencias.model.AnularTasa.AnularTasaRequest;
import com.gamq.licencias.model.AnularTasa.AnularTasaResponse;
import com.gamq.licencias.model.AnularTasa.AnularTasaToken;
import com.gamq.licencias.model.Contribuyente.ContribuyenteRequest;
import com.gamq.licencias.model.Contribuyente.ContribuyenteResponse;
import com.gamq.licencias.model.Contribuyente.ContribuyenteToken;
import com.gamq.licencias.model.Tasa.TasaRequest;
import com.gamq.licencias.model.Tasa.TasaResponse;
import com.gamq.licencias.model.Tasa.TasaToken;
import com.gamq.licencias.model.login.LoginRequest;
import com.gamq.licencias.model.login.LoginResponse;
import com.gamq.licencias.service.AbstractClient;
import com.gamq.licencias.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ApiServiceImpl extends AbstractClient implements ApiService {

    public ApiServiceImpl(RestTemplate restTemplate) {
        super(restTemplate);
    }
    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        String uri = baseUrl + "/ServiciosRuatJEE-web/api/autentificacion";
        log.info("Going to login {}");
        HttpEntity<Void> requestEntity = new HttpEntity<>(this.datosLogin(loginRequest));
        ResponseEntity<LoginResponse> response = restTemplate.exchange(
                uri, HttpMethod.POST, requestEntity ,LoginResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Successfully user creation: {}", response.getBody());
            return response.getBody();
        }
        log.error("Error in user login - httpStatus was: {}", response.getStatusCode());
        throw new RuntimeException("Error");

    }

    @Override
    public ContribuyenteResponse buscarContribuyente(ContribuyenteToken contribuyenteToken) {
        String uri = baseUrl + "/RuatServiciosWebContribuyentes/contribuyentes/comun/busquedaContribuyente";
        //log.info("Going to buscarContribuyente {}", uri);
        String cadena= contribuyenteToken.getToken();
        ContribuyenteRequest contribuyenteRequest = new ContribuyenteRequest();
        contribuyenteRequest= contribuyenteToken.getContribuyenteRequest();
        HttpEntity<ContribuyenteRequest> requestEntity = new HttpEntity<ContribuyenteRequest>(contribuyenteRequest,loguearse(cadena));

        //log.info("Body+ tokens: ",requestEntity.getHeaders(), requestEntity.getBody());
        ResponseEntity<ContribuyenteResponse> response = restTemplate.exchange(
                uri, HttpMethod.POST, requestEntity ,ContribuyenteResponse.class);

        //log.info("REspuesta en frio:",response.getBody());
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Busqueda Exitosa");
            return response.getBody();
        }
        else {
            log.error("Error en la busqueda", response.getStatusCode());
            throw new RuntimeException("Error");
        }
    }
    @Override
    public TasaResponse crearTasa(TasaToken tasaToken){
        String uri = baseUrl + "/RuatServiciosWebTasasOI/tasasOI/registroTasa";
        //log.info("Going to buscarContribuyente {}", uri);
        String cadena= tasaToken.getToken();
        TasaRequest tasaRequest = new TasaRequest();
        tasaRequest= tasaToken.getTasa();
        HttpEntity<TasaRequest> requestEntity = new HttpEntity<TasaRequest>(tasaRequest,loguearse(cadena));
        ResponseEntity<TasaResponse> response = restTemplate.exchange(
                uri, HttpMethod.POST, requestEntity ,TasaResponse.class);

        //log.info("REspuesta en frio:",response.getBody());
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Creacion de tasa Exitosa");
            return response.getBody();
        }
        else {
            log.error("Error en crear Tasa", response.getStatusCode());
            throw new RuntimeException("Error");
        }


    }
    public AnularTasaResponse anularTasa(AnularTasaToken anularTasaToken){
        String uri = baseUrl + "/RuatServiciosWebTasasOI/tasasOI/anulacionTasa";
        String cadena= anularTasaToken.getToken();
        AnularTasaRequest anularTasaRequest = new AnularTasaRequest();
        anularTasaRequest = anularTasaToken.getAnular();
        HttpEntity<AnularTasaRequest> requestEntity = new HttpEntity<AnularTasaRequest>(anularTasaRequest,loguearse(cadena));
        ResponseEntity<AnularTasaResponse> response = restTemplate.exchange(
                uri, HttpMethod.POST, requestEntity ,AnularTasaResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Anulacion de tasa Exitosa");
            return response.getBody();
        }
        else {

            log.error("Error en Anular Tasa", response.getStatusCode());
            throw new RuntimeException("Error");
        }

    }
}
