package com.cipolflo.server.reservas.exception;

public class ReservaValidacionException extends RuntimeException {

    private final String codigo;

    public ReservaValidacionException(ReservaCodigoError codigo, String descripcion) {
        super(descripcion);
        this.codigo = codigo.name();
    }

    public String getCodigo() {
        return codigo;
    }
}
