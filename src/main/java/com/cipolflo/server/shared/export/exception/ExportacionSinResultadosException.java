package com.cipolflo.server.shared.export.exception;

public class ExportacionSinResultadosException extends RuntimeException {
    public ExportacionSinResultadosException() {
        super("No se encontraron resultados para exportar");
    }
    
}
