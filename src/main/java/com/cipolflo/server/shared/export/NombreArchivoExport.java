package com.cipolflo.server.shared.export;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class NombreArchivoExport {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm");

    private NombreArchivoExport() {
    }

    public static String generar(String prefijo) {
        String fechaHora = LocalDateTime
                .now(ZoneId.systemDefault())
                .format(FORMATTER);

        return prefijo + "_" + fechaHora + ".xlsx";
    }
}
