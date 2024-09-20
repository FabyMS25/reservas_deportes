package com.gamq.deportes.model.Registro;

import lombok.Data;

@Data
public class RegistroToken {
    private String token;
    private RegistroRequest registro;
}
