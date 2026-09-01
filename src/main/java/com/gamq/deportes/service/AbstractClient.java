package com.gamq.deportes.service;

import com.gamq.deportes.model.login.LoginRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

public abstract class AbstractClient {

    @Value("${ruat.base-url:https://aplicaciones.ruat.gob.bo}")
    protected String baseUrl;

    protected final  RestTemplate restTemplate;

    protected AbstractClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected HttpHeaders datosLogin(LoginRequest login) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        //headers.set("Authorization", "Bearer " + token);
        headers.set("usuario",login.getUsuario());
        headers.set("clave",login.getClave());
        return headers;
    }

    protected HttpHeaders loguearse(String token){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

}
