package com.cipolflo.server.servicios.exception;

public class ReservaNoCancelableException extends RuntimeException {

    public ReservaNoCancelableException() {
        super("Solo se pueden cancelar reservas próximas del servicio");
    }
}
