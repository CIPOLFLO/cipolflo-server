package com.cipolflo.server.reservas.domain.enums;

public enum EstadoReserva {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    EN_CURSO("En curso"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String label;

    EstadoReserva(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean esFinalizable() {
        return this == EN_CURSO;
    }

    @Override
    public String toString() {
        return label;
    }
}
