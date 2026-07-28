package com.cipolflo.server.shared.email.exception;

public class DestinatarioNotificacionEmailValidacionException extends RuntimeException {

    private final String codigo;

    public DestinatarioNotificacionEmailValidacionException(String codigo, String descripcion) {
        super(descripcion);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
