package com.cipolflo.server.clientes.exception;

public class SocioNotFoundException extends RuntimeException {
    public SocioNotFoundException(Long id) {
        super("Socio no encontrado con id: " + id);
    }
}
