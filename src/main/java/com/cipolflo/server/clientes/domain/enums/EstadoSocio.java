package com.cipolflo.server.clientes.domain.enums;

public enum EstadoSocio {
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    DE_BAJA("De baja");

    private final String label;

    EstadoSocio(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
