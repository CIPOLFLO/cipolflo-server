package com.cipolflo.server.finanzas.exception;

public class FinanzaNotFoundException extends RuntimeException {

    public FinanzaNotFoundException(Long id) {
        super("Finanza no encontrada con id: " + id);
    }
}