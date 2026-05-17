package com.cipolflo.server.servicios.exception;

public class NombreDuplicadoException extends RuntimeException {

    public NombreDuplicadoException(String nombre) {
        super("El nombre '" + nombre + "' ya está en uso. Por favor, elija otro nombre.");
    }
    
}
