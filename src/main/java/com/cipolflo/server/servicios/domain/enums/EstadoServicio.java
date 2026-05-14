package com.cipolflo.server.servicios.domain.enums;

public enum EstadoServicio {
    HABILITADO,
    DESHABILITADO;

    public Boolean toBoolean() {
        return this == HABILITADO;
    }
}
