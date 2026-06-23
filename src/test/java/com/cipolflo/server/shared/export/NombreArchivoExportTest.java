package com.cipolflo.server.shared.export;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NombreArchivoExportTest {

    @Test
    void deberiaGenerarNombreConFormatoEsperado() {
        String nombre = NombreArchivoExport.generar("finanzas");

        assertTrue(nombre.matches("finanzas_\\d{4}-\\d{2}-\\d{2}_\\d{4}\\.xlsx"));
    }

    @Test
    void generar_conPrefijo_terminaConExtension() {
        String resultado = NombreArchivoExport.generar("clientes");

        assertThat(resultado).endsWith(".xlsx");
    }
}
