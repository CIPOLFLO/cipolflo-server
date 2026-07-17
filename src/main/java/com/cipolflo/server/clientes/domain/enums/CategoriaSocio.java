package com.cipolflo.server.clientes.domain.enums;

public enum CategoriaSocio {
    POLICIA_ACTIVO("Policía activo"),
    POLICIA_RETIRADO("Policía retirado"),
    SOCIO_COMUN("Socio común");

    private final String label;

    CategoriaSocio(String label) {
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