package com.cipolflo.server.documentos.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class DocumentoAzureValidator {

    private static final long MAX_SIZE_BYTES = 4L * 1024 * 1024;

    private static final List<String> TIPOS_PERMITIDOS = List.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/bmp",
            "image/tiff",
            "image/heif"
    );

    public void validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe subir un archivo.");
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("El archivo supera el límite de 4 MB permitido.");
        }

        String contentType = file.getContentType();

        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Formato no permitido: " + contentType + ". Use PDF, JPG, PNG, BMP, TIFF o HEIF."
            );
        }
    }
}