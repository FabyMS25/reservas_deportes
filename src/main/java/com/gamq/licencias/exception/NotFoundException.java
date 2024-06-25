package com.gamq.licencias.exception;

import com.gamq.licencias.exception.enums.EntityType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(EntityType resourceName, String uuid) {
        super(String.format("Elemento no encontrado: %s - UUID: %s", resourceName, uuid));
    }
}
