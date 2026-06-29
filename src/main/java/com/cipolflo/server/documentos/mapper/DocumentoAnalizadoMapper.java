package com.cipolflo.server.documentos.mapper;

import com.cipolflo.server.documentos.dto.DocumentoAnalizadoResponseDto;
import com.cipolflo.server.documentos.domain.DocumentoAnalizado;
import org.springframework.stereotype.Component;

@Component
public class DocumentoAnalizadoMapper {

    public DocumentoAnalizadoResponseDto toResponseDto(DocumentoAnalizado documento) {
        return new DocumentoAnalizadoResponseDto(
                documento.getId(),
                documento.getNombreArchivo(),
                documento.getTipoContenido(),
                documento.getModeloUsado(),
                documento.getFechaAnalisis(),
                documento.getResultadoJson()
        );
    }
}