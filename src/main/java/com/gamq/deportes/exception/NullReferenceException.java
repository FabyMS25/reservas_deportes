package com.gamq.deportes.exception;

import com.gamq.deportes.enums.EntityType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class NullReferenceException extends RuntimeException {
    public NullReferenceException(EntityType resourceName) {
        super(String.format("Objeto %s null: ", resourceName));
    }
}
