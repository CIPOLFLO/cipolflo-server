package com.cipolflo.server.servicios.exception;

public class ServicioNotFoundException extends RuntimeException{

    public ServicioNotFoundException(Long id) {
        super("Servicio no encontrado con id: " + id);
    }

}
