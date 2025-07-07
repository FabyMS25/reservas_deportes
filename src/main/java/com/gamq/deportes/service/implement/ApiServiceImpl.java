package com.gamq.deportes.service.implement;

import com.gamq.deportes.model.AnularTasa.AnularTasaRequest;
import com.gamq.deportes.model.AnularTasa.AnularTasaResponse;
import com.gamq.deportes.model.AnularTasa.AnularTasaToken;
import com.gamq.deportes.model.Contribuyente.ContribuyenteRequest;
import com.gamq.deportes.model.Contribuyente.ContribuyenteResponse;
import com.gamq.deportes.model.Contribuyente.ContribuyenteToken;
import com.gamq.deportes.model.Pagos.PagoRequest;
import com.gamq.deportes.model.Pagos.PagoResponse;
import com.gamq.deportes.model.Pagos.PagoToken;
import com.gamq.deportes.model.Registro.RegistroRequest;
import com.gamq.deportes.model.Registro.RegistroResponse;
import com.gamq.deportes.model.Registro.RegistroToken;
import com.gamq.deportes.model.Tasa.TasaRequest;
import com.gamq.deportes.model.Tasa.TasaResponse;
import com.gamq.deportes.model.Tasa.TasaToken;
import com.gamq.deportes.model.login.LoginRequest;
import com.gamq.deportes.model.login.LoginResponse;
import com.gamq.deportes.service.AbstractClient;
import com.gamq.deportes.service.ApiService;
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
    @Override
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
            return response.getBody();
            //throw new RuntimeException("Error");

        }

    }
    @Override
    public RegistroResponse registro(RegistroToken registroToken){

        String uri = baseUrl + "/RuatServiciosWebContribuyentes/contribuyentes/registroContribuyente";
        String cadena= registroToken.getToken();
        RegistroRequest registroRequest = new RegistroRequest();
        registroRequest = registroToken.getRegistro();
        HttpEntity<RegistroRequest> requestEntity = new HttpEntity<RegistroRequest>(registroRequest,loguearse(cadena));
        ResponseEntity<RegistroResponse> response = restTemplate.exchange(
                uri, HttpMethod.POST, requestEntity ,RegistroResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Registro Exitoso !! ");
            return response.getBody();
        }
        else {
            log.error("Error en el Registro", response.getStatusCode());
            return response.getBody();
            //throw new RuntimeException("Error");

        }
    }

    @Override
    public PagoResponse consultaPago (PagoToken pagoToken){
        String uri = baseUrl + "/RuatServiciosWebTasasOI/tasasOI/consultaPagoTasa";
        String cadena= pagoToken.getToken();
        PagoRequest pagoRequest = new PagoRequest();
        pagoRequest = pagoToken.getPago();
        HttpEntity<PagoRequest> requestEntity = new HttpEntity<PagoRequest>(pagoRequest,loguearse(cadena));
        ResponseEntity<PagoResponse> response = restTemplate.exchange(
                uri, HttpMethod.POST, requestEntity ,PagoResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Consulta Realizada !! ");
            return response.getBody();
        }
        else {
            log.error("No hay pagos", response.getStatusCode());
            return response.getBody();
            //throw new RuntimeException("Error");

        }


    }
}
