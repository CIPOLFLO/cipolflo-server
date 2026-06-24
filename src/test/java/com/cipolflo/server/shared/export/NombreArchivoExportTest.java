package com.cipolflo.server.shared.export;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NombreArchivoExportTest {

    @Test
    void deberiaGenerarNombreConFormatoEsperado() {
        String nombre = NombreArchivoExport.generar("finanzas");

        assertTrue(nombre.matches("finanzas_\\d{4}-\\d{2}-\\d{2}_\\d{4}\\.xlsx"));
    }
}