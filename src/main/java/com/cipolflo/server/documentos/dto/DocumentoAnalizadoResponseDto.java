package com.cipolflo.server.documentos.dto;


import java.time.LocalDate;

public record DocumentoAnalizadoResponseDto(
        Long id,
        String nombreArchivo,
        String tipoContenido,
        String modeloUsado,
        LocalDate fechaAnalisis,
        String resultadoJson
) {
}
