package com.cipolflo.server.integraciones.telegram.exception;

public class TelegramValidacionException extends RuntimeException {

    private final String codigo;

    public TelegramValidacionException(String codigo, String descripcion) {
        super(descripcion);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
