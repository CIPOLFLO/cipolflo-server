package com.cipolflo.server.manuales.exception;

/**
 * El manual existe en el catálogo pero su PDF todavía no fue publicado.
 *
 * <p>Se distingue de {@link ManualNoEncontradoException} para que el front pueda avisar
 * "manual en preparación" en lugar de tratarlo como una URL rota.</p>
 */
public class ManualNoDisponibleException extends RuntimeException {

    public ManualNoDisponibleException(String titulo) {
        super("El manual '" + titulo + "' todavía no está disponible para descarga");
    }
}
