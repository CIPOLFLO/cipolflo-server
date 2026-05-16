package com.cipolflo.server.servicios.exception;

public class ServicioValidacionException extends RuntimeException {

    private final String codigo;

    public ServicioValidacionException(String codigo, String descripcion) {
        super(descripcion);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
