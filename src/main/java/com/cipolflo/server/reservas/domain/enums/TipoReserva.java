package com.cipolflo.server.reservas.domain.enums;

public enum TipoReserva {
    COMUN("Común"),
    COLABORACION_SIN_FINES_DE_LUCRO("Colaboración sin fines de lucro");

    private final String label;

    TipoReserva(String label) {
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
