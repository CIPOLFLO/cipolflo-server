package com.cipolflo.server.clientes.dto;

import lombok.Getter;

@Getter
public class FilaErrorImportacionDto {

    private final int numeroFila;
    private final String codigoError;
    private final String motivo;

    public FilaErrorImportacionDto(int numeroFila, String codigoError, String motivo) {
        this.numeroFila = numeroFila;
        this.codigoError = codigoError;
        this.motivo = motivo;
    }
}
