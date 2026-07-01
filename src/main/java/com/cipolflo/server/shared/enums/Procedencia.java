package com.cipolflo.server.shared.enums;

public enum Procedencia {
    SEDE("Sede"),
    CAMPING("Camping"),
    AMBOS("Ambos");

    private final String label;

    Procedencia(String label) {
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
