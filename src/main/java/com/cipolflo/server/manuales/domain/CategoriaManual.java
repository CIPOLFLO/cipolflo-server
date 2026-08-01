package com.cipolflo.server.manuales.domain;

/**
 * Categoría a la que pertenece un manual.
 *
 * <p>Cada categoría define la subcarpeta del classpath donde viven sus PDF,
 * dentro de {@code src/main/resources/manuales/}.</p>
 */
public enum CategoriaManual {

    TECNICO("tecnicos"),
    USUARIO("usuario");

    private final String carpeta;

    CategoriaManual(String carpeta) {
        this.carpeta = carpeta;
    }

    public String getCarpeta() {
        return carpeta;
    }
}
