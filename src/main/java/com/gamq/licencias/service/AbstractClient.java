package com.gamq.licencias.service;

import com.gamq.licencias.model.login.LoginRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractClient {

    @Value("https://verificacionjboss.ruat.gob.bo")
    protected String baseUrl;

    protected final  RestTemplate restTemplate;

    protected AbstractClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected HttpHeaders datosLogin(LoginRequest login) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        //headers.set("Authorization", "Bearer " + token);
        headers.set("usuario",login.getUsuario());
        headers.set("clave",login.getClave());
        return headers;
    }

    protected HttpHeaders loguearse(String token){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

}
