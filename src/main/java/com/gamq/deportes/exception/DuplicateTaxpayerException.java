package com.gamq.deportes.exception;

import java.util.List;

public class DuplicateTaxpayerException extends RuntimeException {
    private final List<String> fields;

    public DuplicateTaxpayerException(List<String> fields) {
        super("Ya existe un contribuyente registrado con " + String.join(", ", fields) + ".");
        this.fields = fields;
    }

    public List<String> getFields() {
        return fields;
    }
}
