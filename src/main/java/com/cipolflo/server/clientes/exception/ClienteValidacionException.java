package com.cipolflo.server.clientes.exception;

public class ClienteValidacionException extends RuntimeException {

    private final String codigo;

    public ClienteValidacionException(String codigo, String descripcion) {
        super(descripcion);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
