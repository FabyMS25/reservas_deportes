package com.gamq.deportes.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamq.deportes.model.AnularTasa.AnularTasaRequest;
import com.gamq.deportes.model.AnularTasa.AnularTasaResponse;
import com.gamq.deportes.model.AnularTasa.AnularTasaToken;
import com.gamq.deportes.model.Contribuyentes.ContribuyenteRequest;
import com.gamq.deportes.model.Contribuyentes.ContribuyenteResponse;
import com.gamq.deportes.model.Contribuyentes.ContribuyenteToken;
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
import com.gamq.deportes.service.ConfiguracionSistemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ApiServiceImpl extends AbstractClient implements ApiService {

    private static final String CODIGO_ALCALDIA_RUAT = "CODIGO_ALCALDIA_RUAT";
    private static final String SERVICIO_MUNICIPAL_RUAT = "SERVICIO_MUNICIPAL_RUAT";
    private static final String USUARIO_RUAT = "USUARIO_RUAT";
    private static final String CLAVE_RUAT = "CLAVE_RUAT";
    private static final String CODIGO_USUARIO_RUAT = "CODIGO_USUARIO_RUAT";
    private static final String TIPO_TASA_RUAT = "TO";

    private final ConfiguracionSistemaService configuracionSistemaService;

    public ApiServiceImpl(RestTemplate restTemplate, ConfiguracionSistemaService configuracionSistemaService) {
        super(restTemplate);
        this.configuracionSistemaService = configuracionSistemaService;
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
    public LoginResponse loginWithConfiguredCredentials() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsuario(requiredTextConfig(USUARIO_RUAT));
        loginRequest.setClave(requiredTextConfig(CLAVE_RUAT));
        return login(loginRequest);
    }

    @Override
    public JsonNode buscarContribuyente(ContribuyenteToken contribuyenteToken) {
        String uri = baseUrl + "/RuatServiciosWebContribuyentes/contribuyentes/comun/busquedaContribuyente";
        //log.info("Going to buscarContribuyente {}", uri);
        String cadena = configuredToken();
        ContribuyenteRequest contribuyenteRequest = contribuyenteToken.getContribuyenteRequest();
        if (contribuyenteRequest == null) {
            contribuyenteRequest = new ContribuyenteRequest();
        }
        contribuyenteRequest.setCodigoAlcaldia(requiredTextConfig(CODIGO_ALCALDIA_RUAT));
        HttpEntity<ContribuyenteRequest> requestEntity = new HttpEntity<ContribuyenteRequest>(contribuyenteRequest,loguearse(cadena));

        //log.info("Body+ tokens: ",requestEntity.getHeaders(), requestEntity.getBody());
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri, HttpMethod.POST, requestEntity, JsonNode.class);

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
    public JsonNode crearTasa(TasaToken tasaToken){
        String uri = baseUrl + "/RuatServiciosWebTasasOI/tasasOI/registroTasa";
        //log.info("Going to buscarContribuyente {}", uri);
        String cadena = configuredToken();
        TasaRequest tasaRequest = tasaToken.getTasa();
        if (tasaRequest == null) {
            tasaRequest = new TasaRequest();
        }
        tasaRequest.setCodigoAlcaldia(requiredTextConfig(CODIGO_ALCALDIA_RUAT));
        tasaRequest.setCodigoUsuario(requiredTextConfig(CODIGO_USUARIO_RUAT));
        tasaRequest.setServicioMunicipal(requiredTextConfig(SERVICIO_MUNICIPAL_RUAT));
        HttpEntity<TasaRequest> requestEntity = new HttpEntity<TasaRequest>(tasaRequest,loguearse(cadena));
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri, HttpMethod.POST, requestEntity, JsonNode.class);

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
    public JsonNode anularTasa(AnularTasaToken anularTasaToken){
        String uri = baseUrl + "/RuatServiciosWebTasasOI/tasasOI/anulacionTasa";
        String cadena = configuredToken();
        AnularTasaRequest anularTasaRequest = anularTasaToken.getAnular();
        if (anularTasaRequest == null) {
            anularTasaRequest = new AnularTasaRequest();
        }
        anularTasaRequest.setCodigoAlcaldia(requiredTextConfig(CODIGO_ALCALDIA_RUAT));
        anularTasaRequest.setCodigoUsuario(requiredTextConfig(CODIGO_USUARIO_RUAT));
        anularTasaRequest.setTipoTasa(TIPO_TASA_RUAT);
        HttpEntity<AnularTasaRequest> requestEntity = new HttpEntity<AnularTasaRequest>(anularTasaRequest,loguearse(cadena));
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri, HttpMethod.POST, requestEntity, JsonNode.class);

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
    public JsonNode registro(RegistroToken registroToken){

        String uri = baseUrl + "/RuatServiciosWebContribuyentes/contribuyentes/registroContribuyente";
        String cadena = configuredToken();
        RegistroRequest registroRequest = registroToken.getRegistro();
        if (registroRequest == null) {
            registroRequest = new RegistroRequest();
        }
        registroRequest.setCodigoAlcaldia(requiredTextConfig(CODIGO_ALCALDIA_RUAT));
        registroRequest.setCodigoUsuario(requiredTextConfig(CODIGO_USUARIO_RUAT));
        HttpEntity<RegistroRequest> requestEntity = new HttpEntity<RegistroRequest>(registroRequest,loguearse(cadena));
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri, HttpMethod.POST, requestEntity, JsonNode.class);

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
    public JsonNode consultaPago(PagoToken pagoToken){
        String uri = baseUrl + "/RuatServiciosWebTasasOI/tasasOI/consultaPagoTasa";
        String cadena = configuredToken();
        PagoRequest pagoRequest = pagoToken.getPago();
        if (pagoRequest == null) {
            pagoRequest = new PagoRequest();
        }
        pagoRequest.setCodigoAlcaldia(requiredTextConfig(CODIGO_ALCALDIA_RUAT));
        pagoRequest.setTipoTasa(TIPO_TASA_RUAT);
        HttpEntity<PagoRequest> requestEntity = new HttpEntity<PagoRequest>(pagoRequest,loguearse(cadena));
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri, HttpMethod.POST, requestEntity, JsonNode.class);

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

    private String configuredToken() {
        LoginResponse loginResponse = loginWithConfiguredCredentials();
        if (loginResponse == null || loginResponse.getToken() == null || loginResponse.getToken().trim().isEmpty()) {
            throw new IllegalArgumentException("No se pudo autenticar con RUAT usando la configuración del sistema.");
        }
        return loginResponse.getToken();
    }

    private String requiredTextConfig(String clave) {
        String value = configuracionSistemaService.obtenerValorTexto(clave, null);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Falta la configuración requerida: " + clave);
        }
        return value.trim();
    }
}
