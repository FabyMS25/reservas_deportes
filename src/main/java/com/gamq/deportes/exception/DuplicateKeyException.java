package com.gamq.deportes.exception;

import com.gamq.deportes.exception.enums.EntityType;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateKeyException extends CannotCreateTransactionException {

    public DuplicateKeyException(EntityType resourceName) {
        super(String.format("Entidad %s ya fue registrado.", resourceName));
    }
}
