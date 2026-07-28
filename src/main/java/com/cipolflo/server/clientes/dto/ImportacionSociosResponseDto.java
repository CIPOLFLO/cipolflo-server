package com.cipolflo.server.clientes.dto;

import com.cipolflo.server.shared.dto.ResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class ImportacionSociosResponseDto implements ResponseDto {

    private final int totalFilas;
    private final int filasImportadas;
    private final int filasConError;
    private final List<FilaErrorImportacionDto> detalleErrores;

    public ImportacionSociosResponseDto(
            int totalFilas,
            int filasImportadas,
            int filasConError,
            List<FilaErrorImportacionDto> detalleErrores
    ) {
        this.totalFilas = totalFilas;
        this.filasImportadas = filasImportadas;
        this.filasConError = filasConError;
        this.detalleErrores = detalleErrores;
    }
}
