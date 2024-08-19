package com.gamq.licencias.controller;


import com.gamq.licencias.dto.canchasDto;
import com.gamq.licencias.model.AnularTasa.AnularTasaResponse;
import com.gamq.licencias.model.AnularTasa.AnularTasaToken;
import com.gamq.licencias.model.Contribuyente.ContribuyenteRequest;
import com.gamq.licencias.model.Contribuyente.ContribuyenteResponse;
import com.gamq.licencias.model.Contribuyente.ContribuyenteToken;
import com.gamq.licencias.model.Pagos.PagoResponse;
import com.gamq.licencias.model.Pagos.PagoToken;
import com.gamq.licencias.model.Registro.RegistroResponse;
import com.gamq.licencias.model.Registro.RegistroToken;
import com.gamq.licencias.model.Tasa.TasaResponse;
import com.gamq.licencias.model.Tasa.TasaToken;
import com.gamq.licencias.model.login.LoginRequest;
import com.gamq.licencias.model.login.LoginResponse;
import com.gamq.licencias.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.gamq.licencias.dto.response.Response;

import java.util.List;

@RestController
@RequestMapping("api/ruat")
public class ApiController {
    @Autowired
    private  ApiService apiService;
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return this.apiService.login(loginRequest);
    }
    @PostMapping("/buscar")
    public Response Buscar(@RequestBody ContribuyenteToken contribuyenteToken) {
        try{

            ContribuyenteResponse contribuyenteResponse=this.apiService.buscarContribuyente(contribuyenteToken);
            return Response.ok().setPayload(contribuyenteResponse);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @PostMapping("/creartasa")
    public Response Anular(@RequestBody TasaToken tasaToken) {

        try{

            TasaResponse tasaResponse=this.apiService.crearTasa(tasaToken);
            return Response.ok().setPayload(tasaResponse);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping("/anulartasa")
    public Response Anular(@RequestBody AnularTasaToken anularTasaToken) {

        try{

            AnularTasaResponse anularTasaResponse= this.apiService.anularTasa(anularTasaToken);
            return Response.ok().setPayload(anularTasaResponse);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @PostMapping("/registro")
    public Response Registrar(@RequestBody RegistroToken registroToken) {

        try{

            RegistroResponse registroResponse= this.apiService.registro(registroToken);
            return Response.ok().setPayload(registroResponse);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @PostMapping("/pago")
    public Response consultaPago(@RequestBody PagoToken pagoToken) {

        try{

            PagoResponse pagoResponse= this.apiService.consultaPago(pagoToken);
            return Response.ok().setPayload(pagoResponse);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @GetMapping()
    public String cadena(){
        return "prueba";
    }
}
