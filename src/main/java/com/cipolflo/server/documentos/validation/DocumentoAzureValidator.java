package com.cipolflo.server.documentos.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
            "application/pdf", new byte[]{0x25, 0x50},
            "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8},
            "image/png", new byte[]{(byte) 0x89, 0x50},
            "image/bmp", new byte[]{0x42, 0x4D},
            "image/tiff-le", new byte[]{0x49, 0x49},
            "image/tiff-be", new byte[]{0x4D, 0x4D}
    );

    public void validarArchivo(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Debe subir un archivo.");
            }

            if (file.getSize() > MAX_SIZE_BYTES) {
                throw new IllegalArgumentException("El archivo supera el límite de 4 MB permitido.");
            }

            if (!tieneFormatoPermitido(file)) {
                throw new IllegalArgumentException(
                        "Formato no permitido. Use PDF, JPG, PNG, BMP o TIFF."
                );
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo.", e);
        }
    }

    private boolean tieneFormatoPermitido(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            return false;
        }

        byte[] header = file.getInputStream().readNBytes(8);

        return MAGIC_BYTES.values().stream()
                .anyMatch((firma) -> header.length >= firma.length
                        && header[0] == firma[0]
                        && header[1] == firma[1]);
    }
}