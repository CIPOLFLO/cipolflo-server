package com.cipolflo.server.shared.export.exception;

public class LimiteTamanioExportacionException extends RuntimeException {
    public LimiteTamanioExportacionException(long limiteBytes) {
        super("El archivo generado supera el tamaño máximo permitido de " + limiteBytes / (1024 * 1024) + " MB");
    }
}