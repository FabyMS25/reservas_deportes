package com.gamq.licencias.model.login;

import lombok.Data;

@Data
public class LoginRequest {

    private String usuario;
    private String clave;
}
