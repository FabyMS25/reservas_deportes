package com.gamq.licencias.controller;


import com.gamq.licencias.dto.canchasDto;
import com.gamq.licencias.model.AnularTasa.AnularTasaResponse;
import com.gamq.licencias.model.AnularTasa.AnularTasaToken;
import com.gamq.licencias.model.Contribuyente.ContribuyenteRequest;
import com.gamq.licencias.model.Contribuyente.ContribuyenteResponse;
import com.gamq.licencias.model.Contribuyente.ContribuyenteToken;
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
    public ContribuyenteResponse Buscar(@RequestBody ContribuyenteToken contribuyenteToken) {
        return this.apiService.buscarContribuyente(contribuyenteToken);
    }
    @PostMapping("/creartasa")
    public TasaResponse Anular(@RequestBody TasaToken tasaToken) {
        return this.apiService.crearTasa(tasaToken);
    }

    @PostMapping("/anulartasa")
    public AnularTasaResponse Anular(@RequestBody AnularTasaToken anularTasaToken) {
        return this.apiService.anularTasa(anularTasaToken);
    }

    @GetMapping()
   public String cadena(){
        return "prueba";
   }
}
