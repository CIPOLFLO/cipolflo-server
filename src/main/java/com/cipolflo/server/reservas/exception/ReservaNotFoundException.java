package com.cipolflo.server.reservas.exception;

public class ReservaNotFoundException extends RuntimeException {
    public ReservaNotFoundException(String message) {
        super(message);
    }

    public ReservaNotFoundException(Long id) {
        super("No se encontró la reserva con id="+id);
    }
}
