package com.cipolflo.server.reservas.exception;

public class ReservaNotFoundException extends RuntimeException {
    public ReservaNotFoundException(String message) {
        super(message);
    }
}
