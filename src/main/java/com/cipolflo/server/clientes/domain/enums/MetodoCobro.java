package com.cipolflo.server.clientes.domain.enums;

public enum MetodoCobro {
    COBRADORA("Cobradora"),
    DESCUENTO_SALARIAL("Descuento salarial"),
    TRANSFERENCIA("Transferencia"),
    EN_SEDE("En sede"),
    EFECTIVO("Efectivo"),
    DEBITO("Débito");

    private final String label;

    MetodoCobro(String label) {
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
