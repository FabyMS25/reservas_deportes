package com.gamq.deportes.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import com.gamq.deportes.service.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.gamq.deportes.model.Contribuyentes.ContribuyenteToken;
import com.gamq.deportes.model.Contribuyentes.ContribuyenteRequest;
import com.gamq.deportes.model.Tasa.TasaToken;
import com.gamq.deportes.model.Tasa.TasaRequest;
import com.gamq.deportes.model.AnularTasa.AnularTasaToken;
import com.gamq.deportes.model.AnularTasa.AnularTasaRequest;
import com.gamq.deportes.model.Registro.RegistroToken;
import com.gamq.deportes.model.Registro.RegistroRequest;
import com.gamq.deportes.model.Pagos.PagoToken;
import com.gamq.deportes.model.Pagos.PagoRequest;

@Service
public class ApiServiceImpl extends AbstractClient implements ApiService {
    private final ConfiguracionSistemaService configuracionSistemaService;
    public ApiServiceImpl(RestTemplate restTemplate, ConfiguracionSistemaService configuracionSistemaService) {
        super(restTemplate);
        this.configuracionSistemaService = configuracionSistemaService;
        // Manual operations and list reviews share the configured HTTP client.
        // A timeout never authorizes retrying an annulment with an unknown outcome.
    }
    @Override
    public JsonNode checkRuatAvailability() {
        return gatewayAvailability();
    }

    @Override
    public ResponseEntity<byte[]> buscarContribuyente(ContribuyenteToken wrapper) {
        if (wrapper == null || wrapper.getContribuyenteRequest() == null) {
            throw new IllegalArgumentException("Faltan los datos de la solicitud RUAT.");
        }
        ContribuyenteRequest request = wrapper.getContribuyenteRequest();
        return gatewayPost("/taxpayers/search", request);
    }
    @Override
    public ResponseEntity<byte[]> crearTasa(TasaToken wrapper) {
        if (wrapper == null || wrapper.getTasa() == null) {
            throw new IllegalArgumentException("Faltan los datos de la solicitud RUAT.");
        }
        TasaRequest request = wrapper.getTasa();
        request.setCodigoUsuario(requiredTextConfig("CODIGO_USUARIO_RUAT"));
        request.setServicioMunicipal(requiredTextConfig("SERVICIO_MUNICIPAL_RUAT"));
        return gatewayPost("/taxes", request);
    }
    @Override
    public ResponseEntity<byte[]> anularTasa(AnularTasaToken wrapper) {
        if (wrapper == null || wrapper.getAnular() == null) {
            throw new IllegalArgumentException("Faltan los datos de la solicitud RUAT.");
        }
        AnularTasaRequest request = wrapper.getAnular();
        request.setCodigoUsuario(requiredTextConfig("CODIGO_USUARIO_RUAT"));
        return gatewayPost("/taxes/annulments", request);
    }
    @Override
    public ResponseEntity<byte[]> registro(RegistroToken wrapper) {
        if (wrapper == null || wrapper.getRegistro() == null) {
            throw new IllegalArgumentException("Faltan los datos de la solicitud RUAT.");
        }
        RegistroRequest request = wrapper.getRegistro();
        request.setCodigoUsuario(requiredTextConfig("CODIGO_USUARIO_RUAT"));
        return gatewayPost("/taxpayers", request);
    }
    @Override
    public ResponseEntity<byte[]> consultaPago(PagoToken wrapper) {
        if (wrapper == null || wrapper.getPago() == null) {
            throw new IllegalArgumentException("Faltan los datos de la solicitud RUAT.");
        }
        PagoRequest request = wrapper.getPago();
        return gatewayPost("/taxes/payments/query", request);
    }
    private String requiredTextConfig(String key) {
        String value = configuracionSistemaService.obtenerValorTexto(key, null);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Falta la configuración requerida: " + key);
        }
        return value.trim();
    }

    @Override
    public ResponseEntity<byte[]> consultaPagoRevision(PagoToken wrapper) {
        if (wrapper == null || wrapper.getPago() == null) {
            throw new IllegalArgumentException("Faltan los datos de consulta de pago");
        }
        return gatewayPost("/taxes/payments/query", wrapper.getPago());
    }

    @Override
    public ResponseEntity<byte[]> anularTasaRevision(AnularTasaToken wrapper) {
        if (wrapper == null || wrapper.getAnular() == null) {
            throw new IllegalArgumentException("Faltan los datos de anulación");
        }
        wrapper.getAnular().setCodigoUsuario(requiredTextConfig("CODIGO_USUARIO_RUAT"));
        return gatewayPost("/taxes/annulments", wrapper.getAnular());
    }
}
