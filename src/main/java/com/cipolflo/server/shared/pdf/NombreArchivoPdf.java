package com.cipolflo.server.shared.pdf;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para generar nombres de archivo de documentos PDF.
 *
 * <p>Análoga a {@code NombreArchivoExport} pero atada a la extensión {@code .pdf}.
 * Cada familia de documento (Excel / PDF) tiene su propia utilidad en su propio
 * paquete; ninguna necesita conocer la extensión de la otra.</p>
 */
public final class NombreArchivoPdf {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm");

    private NombreArchivoPdf() {
    }

    public static String generar(String prefijo) {
        String fechaHora = LocalDateTime
                .now(ZoneId.systemDefault())
                .format(FORMATTER);

        return prefijo + "_" + fechaHora + ".pdf";
    }
}
