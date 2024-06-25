package com.gamq.licencias.exception;

import com.gamq.licencias.exception.enums.EntityType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NO_CONTENT)
public class NoResultException extends RuntimeException {
    public NoResultException(EntityType resourceName) {
        super(String.format("No existen resultados por: ", resourceName));
    }
}
