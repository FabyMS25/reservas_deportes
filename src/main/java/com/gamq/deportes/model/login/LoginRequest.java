package com.gamq.deportes.model.login;

import lombok.Data;

@Data
public class LoginRequest {

    private String usuario;
    private String clave;
}
