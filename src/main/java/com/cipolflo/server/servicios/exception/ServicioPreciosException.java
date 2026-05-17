package com.cipolflo.server.servicios.exception;

public class ServicioPreciosException extends RuntimeException {

    public ServicioPreciosException(String mensaje) {
        super("Precio de socio no puede ser mayor que el precio particular. " + mensaje);
    }
    
}
