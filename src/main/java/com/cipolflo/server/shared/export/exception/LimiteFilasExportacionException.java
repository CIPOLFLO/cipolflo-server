package com.cipolflo.server.shared.export.exception;

public class LimiteFilasExportacionException extends RuntimeException {
    public LimiteFilasExportacionException(int maxFilas) {
        super("El número de filas a exportar excede el límite permitido: " + maxFilas);
    }
}
